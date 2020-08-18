package com.gonglian.webserver.core.connector.http;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.gonglian.webserver.core.constnt.ContextConstant.DEFAULT_CONTENT_TYPE;

public class HttpResponse {

    private int status = 200;
    private String message = "OK";
    private String contentType = DEFAULT_CONTENT_TYPE;
    private int contentLength = -1;
    private String characterEncoding = "utf-8";
    private Map<String, String> headers = new HashMap<>();

    // 状态行和响应头域是否已经写入到发送缓冲区中
    private boolean committed = false;

    private ActionHook hook;

    public void setHook(ActionHook hook){
        this.hook = hook;
    }

    public void action(ActionCode action, Object param) {
        if (hook != null) {
            if (param == null)
                hook.action(action, this);
            else
                hook.action(action, param);
        }
    }

    /**
     * 写入响应体数据
     */
    public void doWrite(ByteBuffer byteBuffer){
        action(ActionCode.WRITE_BODY, byteBuffer);
    }

    /**
     * 状态行和响应头域是否已经写入到发送缓冲区中
     */
    public boolean isCommitted(){
        return committed;
    }

    public void setCommitted(boolean committed){
        this.committed = committed;
    }

    public void reset(){
        if (committed) {
            throw new IllegalStateException();
        }
        recycle();
    }

    public void recycle() {
        committed = false;
        status = 200;
        message = "OK";
        contentType = DEFAULT_CONTENT_TYPE;
        characterEncoding = "utf-8";
        contentLength = -1;
        headers.clear();
    }

    public void addHeader(String name, String value){
        setHeader(name, value);
    }

    public void setHeader(String key, String value){
        headers.put(key, value);
    }

    public String getHeader(String key){
        return headers.get(key);
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public int getStatus(){
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    public String getContentType(){
        return contentType;
    }

    public void setContentLength(int contentLength){
        this.contentLength = contentLength;
    }

    public int getContentLength(){
        if(contentLength > 0){
            return contentLength;
        }
        String temp = getHeader("Content-Length");
        if(temp != null){
            return Integer.valueOf(temp);
        }
        return -1;
    }
}
