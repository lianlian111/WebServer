package com.gonglian.webserver.core.connector.acceptor.aio;

import com.gonglian.webserver.core.connector.endpoint.aio.AioEndpoint;
import com.gonglian.webserver.core.connector.wrapper.aio.AioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * AioAcceptor实现了CompletionHandler接口，连接成功或者失败都会触发该接口的方法
 *
 * CompletionHandler<AsynchronousSocketChannel, Void>
 * 其中AsynchronousSocketChannel就代表该CompletionHandler处理器在处理连接成功时的result是AsynchronousSocketChannel的实例。
 *
 */
@Slf4j
public class AioAcceptor implements CompletionHandler<AsynchronousSocketChannel, Void> {

    private AioEndpoint aioEndpoint;

    public AioAcceptor(AioEndpoint aioEndpoint){
        this.aioEndpoint = aioEndpoint;
    }

    /**
     * 当IO完成时触发该方法
     * @param client 代表IO操作返回的对象
     * @param attachment 发起IO操作时传入的附加参数
     */
    @Override
    public void completed(AsynchronousSocketChannel client, Void attachment) {
        //继续调用accept()方法，接收来自其他客户端的连接，最终形成一个循环
        aioEndpoint.accept();
        //读取请求信息
        AioSocketWrapper aioSocketWrapper = new AioSocketWrapper(client,aioEndpoint);
        try {
            aioSocketWrapper.read(aioSocketWrapper.getReadBuffer(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("accept succeed...");
    }

    /**
     * 当IO失败时触发该方法
     * @param exc IO操作失败引发的异常或错误
     * @param attachment 发起IO操作时传入的附加参数
     */
    @Override
    public void failed(Throwable exc, Void attachment) {
        log.info("accept failed...");
        exc.printStackTrace();
    }
}
