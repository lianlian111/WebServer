package com.gonglian.webserver.sample.web.servlet;


import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.servlet.impl.HttpServlet;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class LogoutServlet extends HttpServlet {
    
    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        request.getApplicationRequestDispatcher("/views/logout.html").forward(request,response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException, IOException {
        request.getSession().removeAttribute("username");
        request.getSession().invalidateSession();
        response.sendRedirect("/login");
    }
}
