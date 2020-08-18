package com.gonglian.webserver.core.connector.acceptor.nio;

import com.gonglian.webserver.core.connector.endpoint.nio.NioEndpoint;
import com.gonglian.webserver.core.connector.wrapper.nio.NioSocketWrapper;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Getter
public class NioPoller implements Runnable{

    //注册操作
    public static final int OP_REGISTER = 0x100;
    private volatile boolean close = false;
    //检查超时的最短时间间隔
    private long nextExpiration = 0;

    private NioEndpoint endpoint;
    //轮询注册的SocketChannel的I/O操作是否就绪
    private Selector selector;
    //是一个非阻塞式（CAS原子操作）的基于链接节点的无界线程安全队列
    private Queue<PollerEvent> events = new ConcurrentLinkedQueue<>();


    public NioPoller(NioEndpoint endpoint) throws IOException {
        this.selector = Selector.open();
        this.endpoint = endpoint;
    }

    public void register(NioSocketWrapper socketWrapper) {
        socketWrapper.setInterestOps(OP_REGISTER);
        addEvent(socketWrapper, OP_REGISTER);
    }

    public void addEvent(NioSocketWrapper socketWrapper, int interestOps){
        PollerEvent pollerEvent = new PollerEvent(socketWrapper, interestOps);
        events.offer(pollerEvent);
        // 某个线程调用select()方法后阻塞了，即使没有通道已经就绪，也有办法让其从select()方法返回。
        // 只要让其它线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可。
        // 阻塞在select()方法上的线程会立马返回。
        selector.wakeup();
    }

    public void close() throws IOException {
        events.clear();
        selector.close();
    }

    @Override
    public void run() {
        int keyCount = 0;
        while (endpoint.isRunning()) {
            boolean hasEvents = false;
            try{
                if(!close){
                    hasEvents = events();
                    keyCount = selector.select(5000);
                }
                if(close){
                    timeout(keyCount, hasEvents);
                    selector.close();
                    break;
                }
            } catch (IOException e) {
                log.error("", e);
                continue;
            }
            // 超时或者被 wakeup
            if(keyCount == 0){
                hasEvents = hasEvents | events();
            }
            Iterator<SelectionKey> iterator = keyCount > 0 ? selector.selectedKeys().iterator() : null;
            while(iterator != null && iterator.hasNext()){
                SelectionKey key = iterator.next();
                NioSocketWrapper socketWrapper = (NioSocketWrapper) key.attachment();
                if(socketWrapper != null){
                    processKey(key, socketWrapper);
                }
                iterator.remove();
            }
            timeout(keyCount, hasEvents);
        }
    }

    private void processKey(SelectionKey selectionKey, NioSocketWrapper socketWrapper) {
        try{
            if(selectionKey.isValid() && socketWrapper != null){
                socketWrapper.setLastAccess();
                int ops = selectionKey.interestOps() & ~(selectionKey.readyOps());
                selectionKey.interestOps(ops);
                socketWrapper.setInterestOps(ops);

                if(socketWrapper.getReadLatch() != null){
                    log.debug("模拟阻塞读 - 通道 {} 已可读", socketWrapper.getSocketChannel());
                    socketWrapper.getReadLatch().countDown();
                }

                if(socketWrapper.getWriteLatch() != null){
                    log.debug("模拟阻塞写 - 通道 {} 已可写", socketWrapper.getWriteLatch());
                    socketWrapper.getWriteLatch().countDown();
                }
                boolean isProcessed = endpoint.execute(socketWrapper);
                if(!isProcessed){
                    log.debug("关闭通道 {} 连接", socketWrapper.getSocketChannel());
                    cancelledKey(selectionKey, socketWrapper);
                }
            }else{
                cancelledKey(selectionKey, socketWrapper);
            }
        }catch (CancelledKeyException e){
            cancelledKey(selectionKey, socketWrapper);
        }
    }

    public void cancelledKey(SelectionKey selectionKey, NioSocketWrapper socketWrapper) {
        if(selectionKey != null){
            selectionKey.attach(null);
            if(selectionKey.isValid()){
                selectionKey.cancel();
            }
        }
        socketWrapper.close();
        endpoint.release();
    }

    private boolean events() {
        log.info("Queue的大小为{},清空Queue,将连接到的socket注册到Selector中", events.size());
        boolean result = false;
        PollerEvent pollerEvent;
        for (int i = 0, size = events.size(); i < size && (pollerEvent = events.poll()) != null; i++) {
            result = true;
            NioSocketWrapper socketWrapper = pollerEvent.getSocketWrapper();
            int interestOps = pollerEvent.getInterestOps();
            if(interestOps == OP_REGISTER){
                try {
                    socketWrapper.getSocketChannel().register(socketWrapper.getNioPoller().getSelector(), SelectionKey.OP_READ, socketWrapper);
                    socketWrapper.setInterestOps(SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }else if(interestOps == SelectionKey.OP_READ || interestOps == SelectionKey.OP_WRITE){
                SelectionKey key = socketWrapper.getSocketChannel().keyFor(socketWrapper.getNioPoller().getSelector());
                if(key == null){
                    socketWrapper.close();
                }else{
                    int ops = key.interestOps() | interestOps;
                    key.interestOps(ops);
                    socketWrapper.setInterestOps(ops);
                }
            }
        }
        return result;
    }

    /**
     * 当满足以下条件时，才执行处理超时：<br>
     * - select() 调用超时（表示负载不大）<br>
     * - nextExpiration 时间已过<br>
     * - server socket 正在关闭
     *
     * @param keyCount
     *            大于0，有 I/O 事件发生，否则没有
     * @param hasEvents
     *            true events队列有事件处理；false 事件队列为空
     */
    private void timeout(int keyCount, boolean hasEvents) {
        long now = System.currentTimeMillis();
        if(nextExpiration > 0 && (keyCount > 0 || hasEvents) && (now < nextExpiration)){
            return;
        }
        for(SelectionKey key : selector.keys()){
            try{
                NioSocketWrapper socketWrapper = (NioSocketWrapper) key.attachment();
                if(socketWrapper == null){
                    cancelledKey(key, socketWrapper);
                }else if((socketWrapper.getInterestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ
                        || (socketWrapper.getInterestOps() & SelectionKey.OP_WRITE )== SelectionKey.OP_WRITE){
                    long delta = now - socketWrapper.getLastAccess();
                    boolean isTimeout = delta > socketWrapper.getTimeout();
                    if(isTimeout){
                        log.info("通道 {} 读或写超时", socketWrapper.getSocketChannel());
                        key.interestOps(0);
                        socketWrapper.setInterestOps(0);
                        cancelledKey(key, socketWrapper);
                    }
                }
            }catch (CancelledKeyException e){
                log.debug("", e);
                cancelledKey(key, (NioSocketWrapper) key.attachment());
            }
        }
        nextExpiration = System.currentTimeMillis() + 1000;
    }

//    public Selector getSelector() {
//        return selector;
//    }

    @Data
    public static class PollerEvent{

        private NioSocketWrapper socketWrapper;
        private int interestOps;

        public PollerEvent(NioSocketWrapper socketWrapper, int interestOps) {
            this.socketWrapper = socketWrapper;
            this.interestOps = interestOps;
        }
    }
}
