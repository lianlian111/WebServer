package com.gonglian.webserver.core.connector.http;

import com.gonglian.webserver.core.context.WebApplication;
import com.gonglian.webserver.core.exception.RequestInvalidException;
import com.gonglian.webserver.core.exception.ServerErrorException;
import com.gonglian.webserver.core.connector.handler.Adapter;
import com.gonglian.webserver.core.connector.wrapper.SocketWrapper;
import com.gonglian.webserver.core.connector.wrapper.nio.NioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class HttpProcessor implements ActionHook {

    private HttpInputBuffer httpInputBuffer;
    private HttpOutputBuffer httpOutputBuffer;

    private HttpRequest httpRequest;
    private HttpResponse httpResponse;

    private Adapter adapter;

    private boolean keepAlive = true;
    private boolean error = false;

    //一个连接最多能处理的请求数
    private int maxKeepAliveRequests = 10;

    public HttpProcessor(){
        httpRequest = new HttpRequest();
        httpResponse = new HttpResponse();
        httpRequest.setHook(this);
        httpResponse.setHook(this);
        httpInputBuffer = new HttpInputBuffer(httpRequest);
        httpOutputBuffer = new HttpOutputBuffer(httpResponse);
    }

    public SocketState process(SocketWrapper socketWrapper){
        httpInputBuffer.setSocketWrapper(socketWrapper);
        httpOutputBuffer.setSocketWrapper(socketWrapper);
        while(!error && keepAlive){
            //解析请求信息
            try{
                if(!httpInputBuffer.parseRequest()){
                    return SocketState.LONG;
                }
            }catch (IOException | RequestInvalidException e) {
                e.printStackTrace();
                if(e instanceof IOException){
                    WebApplication.getExceptionHandler().handle(new ServerErrorException(), httpResponse);
                }
                return SocketState.CLOSED;
            }

            //校验请求头数据
            checkRequest();

            //校验连接数
            if(socketWrapper instanceof NioSocketWrapper){
                if(maxKeepAliveRequests == 1){
                    keepAlive = false;
                }else if(maxKeepAliveRequests > 0 && socketWrapper.decrementKeepAliveLeft()<=0){
                    keepAlive = false;
                }
            }

            if(!error){
                try {
                    adapter.service(httpRequest, httpResponse);
                } catch (IOException e) {
                    WebApplication.getExceptionHandler().handle(new ServerErrorException(), httpResponse);
                    error = true;
                    e.printStackTrace();
                }
            }
            try {
                //flush了一次，此次主要是确保httpOutputBuffer中的数据已经完全发送出去，这样后面关闭连接时不会发生异常
                //此处的end()方法只是为了将写缓冲区的数据全部写出去。
                httpOutputBuffer.end();
                log.info("httpOutputBuffer end");
            } catch (IOException e) {
                WebApplication.getExceptionHandler().handle(new ServerErrorException(), httpResponse);
                error = true;
                e.printStackTrace();
            }
            httpInputBuffer.recycle();
            httpOutputBuffer.recycle();

            if (!error && keepAlive) {
                return SocketState.OPEN;
            }
        }
        return SocketState.CLOSED;
    }

    private void checkRequest(){
        //检查协议版本
        String protocolVersion = httpRequest.getProtocol();
        if(!"HTTP/1.1".equalsIgnoreCase(protocolVersion)){
            error = true;
        }

        //检查是否保持连接
        String connection = httpRequest.getHeader("Connection");
        if(connection == null || connection.equals("Close")){
            keepAlive = false;
        }else if(connection.equals("keep-alive")){
            keepAlive = true;
        }

        //检查host
        String host = httpRequest.getHeader("Host");
        if(host == null || host.length()<0){
            error = true;
        }
    }

    public void setAdapter(Adapter adapter){
        this.adapter = adapter;
    }

    @Override
    public void action(ActionCode actionCode, Object... param) {
        switch (actionCode) {
            case COMMIT:
                if (!httpResponse.isCommitted()) {
                    httpOutputBuffer.commit();
                }
                break;
            case CLOSE:
                action(ActionCode.COMMIT);
                try {
                    httpOutputBuffer.end();
                } catch (IOException e) {
                    error = true;
                    e.printStackTrace();
                }
                break;
            case WRITE_BODY:
                action(ActionCode.COMMIT);
                try {
                    httpOutputBuffer.writeBody((ByteBuffer) param[0]);
                } catch (IOException e) {
                    error = true;
                    e.printStackTrace();
                }
                break;
            case FLUSH:
                action(ActionCode.COMMIT);
                try {
                    httpOutputBuffer.flush();
                } catch (IOException e) {
                    error = true;
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
