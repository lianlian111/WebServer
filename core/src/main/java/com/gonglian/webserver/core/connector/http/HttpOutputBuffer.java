package com.gonglian.webserver.core.connector.http;

import com.gonglian.webserver.core.constnt.CharsetProperties;
import com.gonglian.webserver.core.connector.wrapper.SocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import static com.gonglian.webserver.core.constnt.CharConstant.BLANK;
import static com.gonglian.webserver.core.constnt.CharConstant.CRLF;

@Slf4j
public class HttpOutputBuffer {

    private SocketWrapper socketWrapper;
    private ByteBuffer byteBuffer;

    private HttpResponse httpResponse;

    public HttpOutputBuffer(HttpResponse httpResponse){
        this.httpResponse = httpResponse;
    }

    public void setSocketWrapper(SocketWrapper socketWrapper){
        this.socketWrapper = socketWrapper;
        byteBuffer = socketWrapper.getWriteBuffer();
        byteBuffer.clear();
    }

    /**
     * 将响应头信息写入到缓冲区
     */
    public void commit(){
        StringBuilder appender = new StringBuilder();
        httpResponse.setCommitted(true);
        int pos = byteBuffer.position();

        //写状态行到缓冲区   HTTP/1.1 200 OK
        appender.append("Http/1.1").append(BLANK);
        int status = httpResponse.getStatus();
        String message = httpResponse.getMessage();
        appender.append(status).append(BLANK).append(message).append(CRLF);

        //将响应头写入缓冲区
        // Date: Sat, 31 Dec 2005 23:59:59 GMT
        appender.append("Date:").append(BLANK).append(new Date()).append(CRLF);
        for(Map.Entry<String, String> entry : httpResponse.getHeaders().entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if("Set-Cookie".equals(key)){
                for(String cookie : value.split(";")){
                    appender.append(key).append(":").append(BLANK).append(cookie).append(CRLF);
                }
            }else{
                appender.append(key).append(":").append(BLANK).append(value).append(CRLF);
            }
        }
        appender.append("Content-Length:").append(BLANK);
        byte[] bytes = appender.toString().getBytes(CharsetProperties.UTF_8_CHARSET);
        byteBuffer.put(bytes);
        log.info("将响应头部 [{}B] 数据写入提交到底层缓冲区", (byteBuffer.position() - pos + 1));
    }

    /**
     * 写入响应体数据
     * @param body
     */
    public void writeBody(ByteBuffer body) throws IOException {
        //确保响应头已写入缓冲区
        if(!httpResponse.isCommitted()){
            httpResponse.action(ActionCode.COMMIT, null);
        }
        if(body.remaining() > 0){
            int contentLength = body.remaining();
            StringBuilder appender = new StringBuilder();
            appender.append(contentLength).append(CRLF).append(CRLF);
            byte[] bytes = appender.toString().getBytes(CharsetProperties.UTF_8_CHARSET);
            byteBuffer.put(bytes);
            write(body);
            log.info("写入响应体数据 [{}B]", contentLength);
        }
    }

    public void end() throws IOException {
        if(!httpResponse.isCommitted()){
            httpResponse.action(ActionCode.COMMIT, null);
        }
        if(byteBuffer.hasRemaining()){
            flush();
        }
    }

    public void write(byte[] b) throws IOException {
        write(ByteBuffer.wrap(b));
    }

    public void write(ByteBuffer buffer) throws IOException {
        write(buffer, false);
    }

    public void write(ByteBuffer buffer, boolean flip) throws IOException {
        if(flip){
            buffer.flip();
        }
        while(buffer.hasRemaining()){
            if(byteBuffer.remaining() == 0){
                socketWrapper.flush();
            }
            byteBuffer.put(buffer);
        }
        buffer.clear();
        // 以防超时
        socketWrapper.setLastAccess();
        log.info("写入响应数据成功");
    }

    public void flush() throws IOException {
        socketWrapper.flush();
    }

    public void recycle() {
        httpResponse.recycle();
        byteBuffer.clear();
    }
}
