package com.gonglian.webserver.sample.web.filter;


import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.filter.Filter;
import com.gonglian.webserver.core.filter.FilterChain;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init() {
        log.info("LogFilter init...");
    }

    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) throws ServletException, IOException {
        log.info("{} before accessed, method is {}", request.getRequestURL(), request.getMethod());
        filterChain.doFilter(request, response);
        log.info("{} after accessed, method is {}", request.getRequestURL(), request.getMethod());
    }

    @Override
    public void destroy() {
        log.info("LogFilter destroy...");
    }
}
