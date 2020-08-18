package com.gonglian.webserver.core.connector.endpoint.aio;

import com.gonglian.webserver.core.connector.acceptor.aio.AioAcceptor;
import com.gonglian.webserver.core.connector.endpoint.EndPoint;
import com.gonglian.webserver.core.connector.handler.aio.AioHandler;
import com.gonglian.webserver.core.connector.wrapper.aio.AioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.*;

@Slf4j
public class AioEndpoint extends EndPoint {

    //创建AsynchronousChannelGroup时，需要绑定的线程池ExecutorService，
    //该线程池负责两个任务：处理IO事件和触发CompletionHandler回调接口。
    private ExecutorService pool;

    //java AIO为TCP通信提供的异步Channel
    private AsynchronousServerSocketChannel serverSocketChannel;
    private AioAcceptor aioAcceptor;

    //用于处理业务的线程池
    private ThreadPoolExecutor executor;

    @Override
    public void start(int port) {
        try {
            createExecutor();
            initServerSocket(port);
            log.info("服务器正常启动");
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("服务器启动失败");
        }
    }


    @Override
    public void close() {
        try {
            executor.shutdown();
            serverSocketChannel.close();
            log.info("服务器正常关闭");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void accept() {
        //开始接受来自客户端请求，连接成功或失败都会触发CompletionHandler（aioAcceptor实现了该接口）对象的相应方法
        serverSocketChannel.accept(null, aioAcceptor);
    }

    public void execute(AioSocketWrapper aioSocketWrapper){
        AioHandler aioHandler = new AioHandler(aioSocketWrapper);
        if(executor != null){
            executor.execute(aioHandler);
        }else{
            aioHandler.run();
        }
    }

    private void initServerSocket(int port) throws IOException {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "channel-group-thread-"+count++);
            }
        };
        //int processors = Runtime.getRuntime().availableProcessors();
        pool = new ThreadPoolExecutor(2,2,1, TimeUnit.SECONDS,new ArrayBlockingQueue<>(200), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

        //AsynchronousChannelGroup是异步Channel的分组管理器，它可以实现资源共享
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(pool);
        serverSocketChannel = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(port));
        aioAcceptor = new AioAcceptor(this);
        //等待来自客户端的请求，建立与客户端的连接
        accept();
    }

    private void createExecutor(){
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "worker-thread-"+count++);
            }
        };
        executor = new ThreadPoolExecutor(10, 200, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
