package com.gonglian.webserver.core.servlet;

import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;

import java.io.IOException;

public interface Servlet {

    void init();

    void service(Request request, Response response) throws IOException, ServletException;

    void destroy();
}
