package com.gonglian.webserver.core.connector.handler;

import com.gonglian.webserver.core.context.Context;
import com.gonglian.webserver.core.context.WebApplication;
import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.exception.FilterNotFoundException;
import com.gonglian.webserver.core.exception.ServerErrorException;
import com.gonglian.webserver.core.exception.ServletNotFoundException;
import com.gonglian.webserver.core.filter.AppFilterChain;
import com.gonglian.webserver.core.connector.http.HttpRequest;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.connector.http.HttpResponse;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Adapter {

    private Context context;
    private Servlet servlet;
    private AppFilterChain filterChain;

    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Request request = new Request();
        Response response = new Response();
        request.setHttpRequest(httpRequest);
        response.setHttpResponse(httpResponse);
        request.setResponse(response);
        response.setHttpRequest(httpRequest);
        //触发监听器 调用对应的监听方法
        context.afterRequestCreated(request);
        try{
            if(mapServletAndFilters(request, response)){
                //调用匹配的servlet中的业务处理方法。
                invoke(request, response);
            }
            //flush了1次
            response.finish();
        }finally {
            request.recycle();
            response.recycle();
            context.afterRequestDestroyed(request);
        }
    }

    private boolean mapServletAndFilters(Request request, Response response){
        request.setContext(context);
        try {
            servlet = context.mapServlet(request.getRequestURL().toString());
            filterChain = AppFilterChain.createFilterChain(request);
        } catch (ServletNotFoundException e) {
            log.debug("匹配 Servlet 失败，响应 404");
            response.setStatus(HttpStatus.NOT_FOUND.getCode());
            WebApplication.getExceptionHandler().handle(e, response.getHttpResponse());
            return false;
        } catch (FilterNotFoundException e) {
            log.debug("匹配 Filter 失败，响应 404");
            response.setStatus(HttpStatus.NOT_FOUND.getCode());
            WebApplication.getExceptionHandler().handle(e, response.getHttpResponse());
            return false;
        }
        return true;
    }

    private void invoke(Request request, Response response){
        try{
            if(filterChain != null){
                filterChain.doFilter(request, response);
            }
            servlet.service(request, response);
        }catch (Exception e){
            log.error("Servlet.service() for servlet [{" +servlet + "}] throw exception",e);
            request.setAttribute("javax.servlet.exception", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
            WebApplication.getExceptionHandler().handle(new ServerErrorException(), response.getHttpResponse());
        }finally {
            filterChain.releaseFilters();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
