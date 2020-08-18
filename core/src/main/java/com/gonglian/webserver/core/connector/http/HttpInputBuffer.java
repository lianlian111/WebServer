package com.gonglian.webserver.core.connector.http;

import com.gonglian.webserver.core.constnt.CharConstant;
import com.gonglian.webserver.core.constnt.CharsetProperties;
import com.gonglian.webserver.core.cookie.Cookie;
import com.gonglian.webserver.core.enumeration.RequestMethod;
import com.gonglian.webserver.core.exception.RequestInvalidException;
import com.gonglian.webserver.core.connector.wrapper.SocketWrapper;
import com.gonglian.webserver.core.connector.wrapper.nio.NioSocketWrapper;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Slf4j
public class HttpInputBuffer {

    // 请求头最大大小 k
    private int maxHeaderSize = 8192;

    private HttpRequest httpRequest;
    private SocketWrapper socketWrapper;
    private ByteBuffer byteBuffer;

    public HttpInputBuffer(HttpRequest httpRequest){
        this.httpRequest = httpRequest;
    }

    public void setSocketWrapper(SocketWrapper socketWrapper){
        this.socketWrapper = socketWrapper;
        byteBuffer  = socketWrapper.getReadBuffer();
    }

    public byte[] read(boolean block) throws IOException {
        if(byteBuffer.limit() > maxHeaderSize){
            throw new IllegalArgumentException("请求头太大");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(socketWrapper.read(byteBuffer, block) > 0){
            byteBuffer.flip();
            baos.write(byteBuffer.array());
        }
        baos.close();
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public boolean parseRequest() throws IOException, RequestInvalidException {
        byte[] bytes = null;
        if(socketWrapper instanceof NioSocketWrapper){
            bytes = read(false);
        }else{
            byteBuffer.flip();
            if(byteBuffer.hasRemaining()){
                bytes = byteBuffer.array();
                byteBuffer.clear();
            }
        }
        if(bytes == null || bytes.length == 0){
            return false;
        }
        String line = new String(bytes, CharsetProperties.UTF_8_CHARSET);
        String[] lines = URLDecoder.decode(line, CharsetProperties.UTF_8).split(CharConstant.CRLF);
        if(lines.length <= 1){
            throw new RequestInvalidException();
        }
        parseHeaders(lines);
        String contentLength = httpRequest.getHeader("Content-Length");
        if(contentLength!=null && !contentLength.equals("0")){
            parseBody(lines[lines.length-1]);
        }
        return true;
    }

    public void parseHeaders(String[] lines){
        log.info("解析请求头");
        String firstLine = lines[0];

        //解析方法
        String[] firstLineSlices = firstLine.split(CharConstant.BLANK);
        httpRequest.setMethod(RequestMethod.valueOf(firstLineSlices[0]));
        log.debug("method:{}", httpRequest.getMethod());

        //解析URL
        String rawURL = firstLineSlices[1];
        //  \\ 为 \ 的转移字符， \? 是 ？的转义字符，所以\\?代表一个?
        String[] urlSlices = rawURL.split("\\?");
        httpRequest.setUrl(urlSlices[0]);
        log.debug("url:{}", httpRequest.getUrl());

        //解析URL参数
        if(urlSlices.length > 1){
            parseParams(urlSlices[1]);
        }
        log.debug("params:{}", httpRequest.getParams());

        //解析协议版本
        if(firstLineSlices.length >= 3){
            httpRequest.setProtocol(firstLineSlices[2]);
        }

        //解析请求头
        String header;
        for(int i=1; i<lines.length; i++){
            header = lines[i];
            if(header.equals("")){
                break;
            }
            int colonIndex = header.indexOf(":");
            String key = header.substring(0,colonIndex);
            String value = header.substring(colonIndex+2);
            httpRequest.addHeader(key, value);
        }
        log.debug("headers{}:" , httpRequest.getHeaders());

        //解析cookie
        Cookie[] cookies;
        if(httpRequest.getHeader("Cookie") != null){
            String[] rawCookies = httpRequest.getHeader("Cookie").split("; ");
            cookies = new Cookie[rawCookies.length];
            for(int i=0; i<rawCookies.length; i++){
                String[] kv = rawCookies[i].split("=");
                cookies[i] = new Cookie(kv[0], kv[1]);
            }
        }else{
            cookies = new Cookie[0];
        }
        httpRequest.setCookies(cookies);
        log.debug("Cookies:{}", Arrays.toString(cookies));
    }

    private void parseParams(String urlSlice) {
        String[] urlParams = urlSlice.split("&");
        for(int i=0; i<urlParams.length; i++){
            if(urlParams[i].startsWith("=")){
                continue;
            }
            String[] kv = urlParams[i].split("=");
            String key = kv[0];
            String value = kv.length>1 ? kv[1] : "";
            httpRequest.getParams().put(key, value);
        }
    }

    private void parseBody(String line) {
        log.info("解析请求体");
        parseParams(line);
    }

    public void recycle(){
        httpRequest.recycle();
        byteBuffer.clear();
    }
}
