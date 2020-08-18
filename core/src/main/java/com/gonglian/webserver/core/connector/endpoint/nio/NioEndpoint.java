package com.gonglian.webserver.core.connector.endpoint.nio;

import com.gonglian.webserver.core.connector.acceptor.nio.NioAcceptor;
import com.gonglian.webserver.core.connector.acceptor.nio.NioPoller;
import com.gonglian.webserver.core.connector.endpoint.EndPoint;
import com.gonglian.webserver.core.connector.handler.nio.NioHandler;
import com.gonglian.webserver.core.connector.wrapper.nio.NioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NioEndpoint extends EndPoint {

    private volatile boolean isRunning = false;
    private int pollerCount = Math.min(2, Runtime.getRuntime().availableProcessors());
    private AtomicInteger pollerIndex = new AtomicInteger(0);

    private ServerSocketChannel serverSocket;
    private NioAcceptor nioAcceptor;
    private List<NioPoller> nioPollers;
    private ThreadPoolExecutor executor = null;

    private Map<SocketChannel, NioSocketWrapper> connections = new ConcurrentHashMap<>();
    private final Map<NioSocketWrapper, NioHandler> nioHandlers = new ConcurrentHashMap<>();

    //控制来自客户端总连接的信号量
    private Semaphore connectionLimit;
    private int maxConnections = 1024;

    @Override
    public void start(int port) {
        try {
            isRunning = true;
            initServerSocket(port);
            initPoller();
            initAcceptor();
            createExecutor();
            log.info("服务器正常启动");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("服务器启动失败");
            close();
        }
    }

    @Override
    public void close() {
        isRunning = false;
        for(NioPoller nioPoller : nioPollers){
            try {
                nioPoller.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("服务器正常关闭");
    }

    public boolean execute(NioSocketWrapper socketWrapper){
        if(socketWrapper == null){
            return false;
        }
        NioHandler nioHandler = nioHandlers.get(socketWrapper);
        if(nioHandler == null){
            nioHandler = new NioHandler(socketWrapper);
            nioHandlers.put(socketWrapper, nioHandler);
        }
        if(executor != null){
            executor.execute(nioHandler);
        }else{
            nioHandler.run();
        }
        return true;
    }


    public void registerToPoller(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        NioPoller poller = getPoller();
        NioSocketWrapper socketWrapper = new NioSocketWrapper(socketChannel, poller);
        poller.register(socketWrapper);
    }

    public boolean isRunning(){
        return isRunning;
    }

    public SocketChannel accept() throws IOException {
        return serverSocket.accept();
    }

    /**
     * 申请一个连接许可
     * @throws InterruptedException
     */
    public void acquire() throws InterruptedException {
        if(maxConnections == -1){
            return;
        }
        connectionLimit.acquire();
    }

    /**
     * 释放一个连接许可
     */
    public void release(){
        if(maxConnections == -1){
            return;
        }
        connectionLimit.release();
    }

    private void initServerSocket(int port) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        //设置ServerSocketChannel为阻塞模式，阻塞接收来自浏览器的请求连接
        serverSocket.configureBlocking(true);
        connectionLimit = new Semaphore(maxConnections);
    }

    private void initAcceptor(){
        nioAcceptor = new NioAcceptor(this);
        Thread t = new Thread(nioAcceptor, "NioAcceptor");
        t.setDaemon(true);
        t.start();
    }

    private void initPoller() throws IOException {
        nioPollers = new ArrayList<>();
        for(int i=0; i<pollerCount; i++){
            String pollerName = "NioPoller-"+i;
            NioPoller nioPoller = new NioPoller(this);
            Thread pollerThread = new Thread(nioPoller, pollerName);
            pollerThread.setDaemon(true);
            pollerThread.start();
            nioPollers.add(nioPoller);
        }
    }

    private void createExecutor(){
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "worker-thread-"+threadNumber.getAndIncrement());
            }
        };
        executor = new ThreadPoolExecutor(10, 200, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), threadFactory,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 轮询Poller，实现负载均衡
     * @return
     */
    private NioPoller getPoller(){
        int index = Math.abs(pollerIndex.getAndIncrement()) % nioPollers.size();
        return nioPollers.get(index);
    }
}
