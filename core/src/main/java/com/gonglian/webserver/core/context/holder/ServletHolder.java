package com.gonglian.webserver.core.context.holder;

import com.gonglian.webserver.core.servlet.Servlet;
import lombok.Data;

@Data
public class ServletHolder {
    private Servlet servlet;
    private String servletClass;

    public ServletHolder(String servletClass){
        this.servletClass = servletClass;
    }
}
