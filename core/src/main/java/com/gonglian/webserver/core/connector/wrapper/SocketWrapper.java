package com.gonglian.webserver.core.connector.wrapper;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class SocketWrapper {

    //超时时间1min
    protected static int timeout = 6*1000;

    //该连接能保持的最大请求数
    protected volatile int keepAliveLeft = 10;
    private long lastAccess;

    protected ByteBuffer readBuffer;
    protected ByteBuffer writeBuffer;

    public SocketWrapper(){
        readBuffer = ByteBuffer.allocate(8192);
        writeBuffer = ByteBuffer.allocate(8192);
        lastAccess = System.currentTimeMillis();
    }

    public abstract int read(ByteBuffer byteBuffer, boolean block) throws IOException;

    public abstract void flush() throws IOException;

    public abstract  void close();



    public ByteBuffer getReadBuffer(){
        return readBuffer;
    }

    public ByteBuffer getWriteBuffer(){
        return writeBuffer;
    }

    public void setKeepAliveLeft(int keepAliveLeft){
        this.keepAliveLeft = keepAliveLeft;
    }
    public int decrementKeepAliveLeft(){
        return --keepAliveLeft;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setLastAccess(){
        lastAccess = System.currentTimeMillis();
    }

    public long getLastAccess(){
        return lastAccess;
    }

}
