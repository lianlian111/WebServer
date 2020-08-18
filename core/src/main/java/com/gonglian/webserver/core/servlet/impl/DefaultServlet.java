package com.gonglian.webserver.core.servlet.impl;

import com.gonglian.webserver.core.enumeration.RequestMethod;
import com.gonglian.webserver.core.exception.ResourceNotFoundException;
import com.gonglian.webserver.core.exception.TemplateResolverException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;

import java.io.IOException;

/**
 * 如果当前url没有匹配任何的servlet，就会调用默认Servlet，它可以处理静态资源
 */
public class DefaultServlet extends HttpServlet {

    @Override
    public void service(Request request, Response response) throws IOException, ResourceNotFoundException, TemplateResolverException {
        if(request.getRequestMethod() == RequestMethod.GET){
            if(request.getRequestURL().toString().equals("/")){
                request.setRequestURL("/index.html");
            }
            request.getApplicationRequestDispatcher(request.getRequestURL().toString()).forward(request, response);
        }
    }
}
