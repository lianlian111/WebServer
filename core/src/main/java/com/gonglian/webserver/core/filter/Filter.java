package com.gonglian.webserver.core.filter;

import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;

import java.io.IOException;

/**
 * 过滤器
 */
public interface Filter {

    /**
     * 过滤器初始化
     */
    void init();

    /**
     * 过滤器销毁
     */
    void destroy();

    /**
     * 过滤
     * @param request
     * @param response
     * @param filterChain
     */
    void doFilter(Request request, Response response, FilterChain filterChain)throws IOException, ServletException;
}
