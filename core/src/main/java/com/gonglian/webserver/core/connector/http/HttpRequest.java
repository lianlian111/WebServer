package com.gonglian.webserver.core.connector.http;

import com.gonglian.webserver.core.context.Context;
import com.gonglian.webserver.core.cookie.Cookie;
import com.gonglian.webserver.core.enumeration.RequestMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpRequest {

    private RequestMethod method;
    private String url;
    private String protocol;
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();
    private Cookie[] cookies;

    private ActionHook hook;

    private Context context;


    public void setHook(ActionHook hook){
        this.hook = hook;
    }

    public void action(ActionCode code, Object param){
        if(hook != null){
            if(param == null){
                hook.action(code, null);
            }else{
                hook.action(code, param);
            }
        }
    }

    public void recycle(){
        method = null;
        url = null;
        protocol = null;
        params.clear();
        headers.clear();
        attributes.clear();
        cookies = null;
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    public String getHeader(String name){
        return headers.get(name);
    }

    public void addHeader(String name, String value){
        headers.put(name, value);
    }

    public String removeHeader(String name){
        return headers.remove(name);
    }

    public Map<String, String> getParams(){
        return params;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Cookie[] getCookies() {
        return cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }
    public void setAttribute(String key, Object value){
        attributes.put(key, value);
    }

    public Object getAttribute(String key){
        return attributes.get(key);
    }

}
