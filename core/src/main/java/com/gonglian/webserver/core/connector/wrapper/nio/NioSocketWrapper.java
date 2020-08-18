package com.gonglian.webserver.core.connector.wrapper.nio;

import com.gonglian.webserver.core.connector.acceptor.nio.NioPoller;
import com.gonglian.webserver.core.connector.wrapper.SocketWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class NioSocketWrapper extends SocketWrapper {

    private final SocketChannel socketChannel;

    private int interestOps;
    private long lastAccess;

    private CountDownLatch readLatch;
    private CountDownLatch writeLatch;

    private final NioPoller nioPoller;

    public NioSocketWrapper(SocketChannel socketChannel, NioPoller nioPoller){
        super();
        this.socketChannel = socketChannel;
        this.nioPoller = nioPoller;
    }

    public void close(){
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int read(ByteBuffer byteBuffer, boolean block) throws IOException {
        int n = 0;
        if(block){
            //模拟阻塞
            boolean timeout = false;
            int keyCount = 1; // 1表示当前时刻能立马读取字节
            long time = System.currentTimeMillis();
            while(!timeout){
                if (keyCount > 0){
                    n = socketChannel.read(byteBuffer);
                    if(n != 0){
                        break;
                    }
                }
                // n等于0，此刻可能因为网络原因不能读取到数据，等待下次可读事件触发读操作
                readLatch = new CountDownLatch(1);
                nioPoller.addEvent(this, SelectionKey.OP_READ);
                try {
                    readLatch.await(this.timeout, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //判断是readLatch自身超时还是读事件触发
                if(readLatch != null && readLatch.getCount() > 0){
                    keyCount = 0;
                    timeout = System.currentTimeMillis()-time >= this.timeout;
                }else{
                    keyCount = 1;
                    readLatch = null;
                }
            }
            if(timeout){
                throw new SocketTimeoutException();
            }
        }else{
            n = socketChannel.read(byteBuffer);
        }
        return n;
    }

    /**
     * 阻塞的将响应体数据发送到客户端
     */
    public void flush() throws IOException {
        writeBuffer.flip();
        if(writeBuffer.remaining() > 0){
            log.debug("模拟阻塞写入将响应体 {}B 数据写入通道{}", writeBuffer.remaining(), this);
        }
        while(writeBuffer.remaining() > 0){
            int count = socketChannel.write(writeBuffer);
            if(count == -1){
                throw new EOFException();
            }
            if(count > 0){
                continue;
            }
            //如果写数据返回值cnt=0，通常是网络不稳定造成的写数据失败
            writeLatch = new CountDownLatch(1);
            nioPoller.addEvent(this, SelectionKey.OP_WRITE);
            try {
                writeLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeLatch = null;
        }
        writeBuffer.clear();
    }


    public int getInterestOps() {
        return interestOps;
    }

    public void setInterestOps(int interestOps) {
        this.interestOps = interestOps;
    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public CountDownLatch getReadLatch() {
        return readLatch;
    }

    public CountDownLatch getWriteLatch() {
        return writeLatch;
    }

    public NioPoller getNioPoller(){
        return nioPoller;
    }
}
