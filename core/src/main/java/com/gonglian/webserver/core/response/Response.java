package com.gonglian.webserver.core.response;

import com.gonglian.webserver.core.cookie.Cookie;
import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.connector.http.HttpResponse;
import com.gonglian.webserver.core.response.output.OutputBuffer;
import com.gonglian.webserver.core.connector.http.HttpRequest;
import com.gonglian.webserver.core.response.output.ServletOutputStream;
import com.gonglian.webserver.core.response.output.ServletWriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Slf4j
@Getter
public class Response {

    private HttpRequest httpRequest;

    private HttpResponse httpResponse;

    private OutputBuffer outputBuffer;
    private ServletOutputStream outputStream;
    private ServletWriter writer;

    private boolean usingOutputStream = false;
    private boolean usingWriter = false;

    private boolean error = false;

    public void addCookie(Cookie cookie) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = cookie.getName()+"=";
        String value = cookie.getValue();
        if(value == null || value.length() == 0){
            value = "\"\"";
        }
        boolean isSet = false;
        String cookies = httpResponse.getHeader("Set-Cookie");
        if(cookies != null){
            if(cookies.contains(prefix)){
                isSet = true;
                for(String temp : cookies.split(";")){
                    if(temp.contains(prefix)){
                        stringBuilder.append(prefix).append(value).append(";");
                    }else{
                        stringBuilder.append(temp).append(";");
                    }
                    stringBuilder.delete(stringBuilder.length()-1, stringBuilder.length());
                }
            }else{
                stringBuilder.append(cookies).append(";");
            }
        }
        if(!isSet){
            stringBuilder.append(prefix).append(value);
        }
        httpResponse.addHeader("Set-Cookie", stringBuilder.toString());
    }

    /**
     * 重定向
     * @param s
     */
    public void sendRedirect(String s){
        setStatus(HttpStatus.MOVED_TEMPORARILY.getCode());
        setHeader("Location", s);
        outputBuffer.setSuspended(true);
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if(usingWriter){
            throw new IllegalStateException("The response is using writer");
        }
        usingOutputStream = true;
        if(outputStream == null){
            outputStream = new ServletOutputStream(outputBuffer);
        }
        return outputStream;
    }

    public PrintWriter getWriter() throws IOException {
        if(usingOutputStream){
            throw new IllegalStateException("The Response is using outputStream");
        }
        usingWriter = true;
        if(writer == null){
            writer = new ServletWriter(outputBuffer);
        }
        return writer;
    }

    public void sendError(int i) throws IOException {
        sendError(i, null);
    }

    public void sendError(int i, String s) throws IOException {
        if(isCommitted()){
            throw new IllegalStateException("Cannot call sendError() after the response has been committed");
        }
        httpResponse.setStatus(i);
        httpResponse.setMessage(s);
        if(isCommitted()){
            throw new IllegalStateException("Response ResetBuffer IllegalStateException");
        }
        outputBuffer.recycle();
        outputBuffer.setSuspended(true);
    }

    public void setHttpRequest(HttpRequest httpRequest){
        this.httpRequest = httpRequest;
    }

    public void setSuspended(boolean suspended){
        outputBuffer.setSuspended(suspended);
    }

    public boolean isCommitted() {
        return httpResponse.isCommitted();
    }

    public boolean isError(){
        return error;
    }

    public void finish() throws IOException {
        outputBuffer.close();
    }


    public void reset() {
        httpResponse.reset();
        recycle();
    }

    public void recycle() {
        outputBuffer.recycle();
        usingOutputStream = false;
        usingWriter = false;
        error = false;
    }

    public void setHttpResponse(HttpResponse httpResponse){
        this.httpResponse = httpResponse;
        if(outputBuffer == null){
            outputBuffer = new OutputBuffer();
            outputBuffer.setHttpResponse(httpResponse);
        }
    }


    public boolean containsHeader(String s) {
        return httpResponse.getHeader(s) != null ;
    }


    public void setHeader(String s, String s1) {
        httpResponse.setHeader(s, s1);
    }


    public void addHeader(String s, String s1) {
        httpResponse.addHeader(s, s1);
    }


    public String getHeader(String s) {
        return httpResponse.getHeader(s);
    }

    public void setStatus(int i) {
        httpResponse.setStatus(i);
    }


    public void setStatus(int i, String s) {
        httpResponse.setStatus(i);
        httpResponse.setMessage(s);
    }


    public int getStatus() {
        return httpResponse.getStatus();
    }


    public void setCharacterEncoding(String s) {
        httpResponse.setCharacterEncoding(s);
    }


    public String getCharacterEncoding() {
        return httpResponse.getCharacterEncoding();
    }


    public void setContentLength(int i) {
        httpResponse.setContentLength(i);
    }


    public void setContentType(String s) {
        httpResponse.setContentType(s);
    }


    public String getContentType() {
        return httpResponse.getContentType();
    }





    public String encodeURL(String s) {
        return null;
    }


    public String encodeRedirectURL(String s) {
        return null;
    }


    public String encodeUrl(String s) {
        return null;
    }


    public String encodeRedirectUrl(String s) {
        return null;
    }


    public void setDateHeader(String s, long l) {

    }


    public void addDateHeader(String s, long l) {

    }


    public void setIntHeader(String s, int i) {

    }


    public void addIntHeader(String s, int i) {

    }


    public Collection<String> getHeaders(String s) {
        return null;
    }


    public Collection<String> getHeaderNames() {
        return null;
    }


    public void setContentLengthLong(long l) {

    }


    public void setBufferSize(int i) {

    }


    public int getBufferSize() {
        return 0;
    }


    public void flushBuffer() throws IOException {

    }


    public void resetBuffer() {
    }


    public void setLocale(Locale locale) {

    }


    public Locale getLocale() {
        return null;
    }
}
