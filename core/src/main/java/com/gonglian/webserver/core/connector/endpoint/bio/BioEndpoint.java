package com.gonglian.webserver.core.connector.endpoint.bio;

import com.gonglian.webserver.core.connector.acceptor.bio.BioAcceptor;
import com.gonglian.webserver.core.connector.endpoint.EndPoint;
import com.gonglian.webserver.core.connector.handler.bio.BioHandler;
import com.gonglian.webserver.core.connector.wrapper.bio.BioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class BioEndpoint extends EndPoint {

    private ServerSocket server;
    private BioAcceptor acceptor;
    private ThreadPoolExecutor executor;
    private volatile boolean isRunning = true;

    @Override
    public void start(int port) {
        try {
            createExecutor();
            server = new ServerSocket(port);
            initAcceptor();
            log.info("服务器正常启动");
        } catch (IOException e) {
            log.info("服务器启动失败");
            close();
        }
    }

    @Override
    public void close() {
        isRunning = false;
        try {
            executor.shutdown();
            server.close();
            log.info("服务器正常关闭");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket accept() throws IOException {
        return server.accept();
    }

    public void execute(BioSocketWrapper bioSocketWrapper){
        BioHandler bioHandler = new BioHandler(bioSocketWrapper);
        if(executor != null){
            executor.execute(bioHandler);
        }else{
            bioHandler.run();
        }
    }

    public boolean isRunning(){
        return isRunning;
    }

    private void initAcceptor(){
        acceptor = new BioAcceptor(this);
        Thread t = new Thread(acceptor, "bio-Acceptor-Thread");
        t.setDaemon(true);
        t.start();
    }

    private void createExecutor(){
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"worker-thread-"+threadNumber.getAndIncrement());
            }
        };
        executor = new ThreadPoolExecutor(10, 200, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
