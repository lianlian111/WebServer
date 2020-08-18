package com.gonglian.webserver.core.connector.handler.nio;

import com.gonglian.webserver.core.context.WebApplication;
import com.gonglian.webserver.core.connector.acceptor.nio.NioPoller;
import com.gonglian.webserver.core.connector.handler.Adapter;
import com.gonglian.webserver.core.connector.http.HttpProcessor;
import com.gonglian.webserver.core.connector.http.SocketState;
import com.gonglian.webserver.core.connector.wrapper.nio.NioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Slf4j
public class NioHandler implements Runnable {

    private NioSocketWrapper nioSocketWrapper;
    private HttpProcessor httpProcessor;
    private Adapter adapter;

    public NioHandler(NioSocketWrapper nioSocketWrapper){
        this.nioSocketWrapper = nioSocketWrapper;
        httpProcessor = new HttpProcessor();
        adapter = new Adapter();
        adapter.setContext(WebApplication.getContext());
        httpProcessor.setAdapter(adapter);
    }

    @Override
    public void run() {
        SocketState state = SocketState.CLOSED;
        state = httpProcessor.process(nioSocketWrapper);
        // 检查处理结果
        if (state == SocketState.LONG) {
            log.debug("请求头数据不完整，通道 {} 重新声明关注读取事件", nioSocketWrapper);
            // 处理期间发现读取的数据不完整，要再次读取，此时通道要再次在 Poller 上声明关注读取事件
            nioSocketWrapper.getNioPoller().addEvent(nioSocketWrapper, SelectionKey.OP_READ);
            // 不会移除通道和处理器的映射关系
        } else if (state == SocketState.OPEN) {
            log.debug("保持连接，通道 {} 重新声明关注读取事件", nioSocketWrapper);
            // 再次声明关注读取事件
            nioSocketWrapper.getNioPoller().addEvent(nioSocketWrapper, SelectionKey.OP_READ);
        } else if (state == SocketState.WRITE) {
            log.debug("写入响应数据，通道 {} 声明关注写入事件",  nioSocketWrapper);
            // 简单起见，这个 Poller 也处理写入事件
            nioSocketWrapper.getNioPoller().addEvent(nioSocketWrapper, SelectionKey.OP_WRITE);
        } else { // Connection closed
            // 关闭连接
            SocketChannel socketChannel = nioSocketWrapper.getSocketChannel();
            NioPoller poller = nioSocketWrapper.getNioPoller();
            poller.cancelledKey(socketChannel.keyFor(poller.getSelector()), nioSocketWrapper);
            nioSocketWrapper = null;
            httpProcessor = null;
        }
    }
}
