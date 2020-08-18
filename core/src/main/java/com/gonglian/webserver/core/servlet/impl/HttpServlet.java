package com.gonglian.webserver.core.servlet.impl;

import com.gonglian.webserver.core.enumeration.RequestMethod;
import com.gonglian.webserver.core.exception.ResourceNotFoundException;
import com.gonglian.webserver.core.exception.TemplateResolverException;
import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.servlet.Servlet;

import java.io.IOException;

public abstract class HttpServlet implements Servlet {
    @Override
    public void init() {

    }

    @Override
    public void service(Request request, Response response) throws IOException, ServletException {
        if(request.getRequestMethod() == RequestMethod.GET){
            doGet(request, response);
        }else if(request.getRequestMethod() == RequestMethod.POST){
            doPost(request, response);
        }else if(request.getRequestMethod() == RequestMethod.PUT){
            doPut(request, response);
        }else if(request.getRequestMethod() == RequestMethod.DELETE){
            doDelete(request, response);
        }
    }

    public void doDelete(Request request, Response response)throws IOException, ServletException {

    }

    public void doPut(Request request, Response response)throws IOException, ServletException {

    }

    public void doPost(Request request, Response response)throws IOException, ServletException {

    }

    public void doGet(Request request, Response response)throws IOException, ServletException {
    }

    @Override
    public void destroy() {

    }
}
