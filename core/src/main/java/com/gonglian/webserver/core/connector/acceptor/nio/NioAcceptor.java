package com.gonglian.webserver.core.connector.acceptor.nio;

import com.gonglian.webserver.core.connector.endpoint.nio.NioEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;

@Slf4j
public class NioAcceptor implements Runnable {

    private NioEndpoint nioEndpoint;

    public NioAcceptor(NioEndpoint nioEndpoint){
        this.nioEndpoint = nioEndpoint;
    }

    @Override
    public void run() {
        while(nioEndpoint.isRunning()){
            try {
                nioEndpoint.acquire();
                SocketChannel client = nioEndpoint.accept();
                try {
                    log.info("Acceptor接收到客户端连接{}", client);
                    nioEndpoint.registerToPoller(client);
                } catch (IOException e) {
                    nioEndpoint.release();
                    client.close();
                }
            } catch (SocketTimeoutException e) {
                log.info("socket timeout", e);
            } catch (Throwable e) {
                log.error("socket accepted fail", e);
            }
        }
    }
}
