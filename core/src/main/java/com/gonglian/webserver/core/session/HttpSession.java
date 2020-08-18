package com.gonglian.webserver.core.session;

import com.gonglian.webserver.core.context.WebApplication;

import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.gonglian.webserver.core.constnt.ContextConstant.DEFAULT_SESSION_EXPIRE_TIME;

public class HttpSession {

    private String id;
    private Map<String, Object> attributes;
    private boolean isValid;

    //用于判断session是否过期，标准为当前时间-上次访问时间 >= 阈值
    private Instant lastAccessed;
    private int maxInactiveInterval;

    public HttpSession(String id){
        this.id = id;
        attributes = new ConcurrentHashMap<>();
        this.isValid = true;
        this.lastAccessed = Instant.now();
        this.maxInactiveInterval = DEFAULT_SESSION_EXPIRE_TIME;
    }

    /**
     * 使当前session失效，之后就无法读写当前session了
     * 并会清除session数据，并且在context中删除此session
     */
    public void invalidateSession(){
        isValid = false;
        attributes.clear();
        WebApplication.getContext().invalidateSession(this);
    }

    public void setAttribute(String key, Object value){
        if(this.isValid){
            lastAccessed = Instant.now();
            attributes.put(key, value);
        }else{
            throw new IllegalStateException("session has invalidated");
        }
    }

    public Object getAttribute(String key){
        if(isValid){
            lastAccessed = Instant.now();
            return attributes.get(key);
        }
        throw new IllegalStateException("session has invalidated");
    }

    public void removeAttribute(String key){
        attributes.remove(key);
    }

    public Instant getLastAccessed(){
        return lastAccessed;
    }

    public void setLastAccessed(){
        lastAccessed = Instant.now();
    }

    public boolean isValid(){
        Instant now = Instant.now();
        //Duration.between()方法表示获取两个临时对象之间的持续事件的Duration
        if(Duration.between(lastAccessed, now).getSeconds()>=DEFAULT_SESSION_EXPIRE_TIME){
            isValid = false;
        }
        return isValid;
    }

    public Enumeration<String> getAttributeNames() {
        Set<String> keySet = attributes.keySet();
        if(keySet.isEmpty()){
            return null;
        }else{
            Enumeration<String> enumeration = new Enumeration<String>() {
                Iterator<String> iterator = keySet.iterator();
                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public String nextElement() {
                    return iterator.next();
                }
            };
            return enumeration;
        }
    }

    public String getId(){
        return id;
    }

    public long getLastAccessedTime() {
        return lastAccessed.getEpochSecond();
    }

    public void setMaxInactiveInterval(int i) {
        this.maxInactiveInterval = i;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public boolean isNew() {
        return false;
    }
}
