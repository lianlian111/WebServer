package com.gonglian.webserver.core.filter;

import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;

import java.io.IOException;

/**
 * 拦截器链
 */
public interface FilterChain {

    /**
     * 当前filter放行，由后续的filter继续进行过滤
     * @param request
     * @param response
     */
    void doFilter(Request request, Response response) throws ServletException, IOException;
}
