package com.gonglian.webserver.sample.web.servlet;


import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.servlet.impl.HttpServlet;
import com.gonglian.webserver.sample.domain.User;
import com.gonglian.webserver.sample.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class UserEditServlet extends HttpServlet {
    private UserService userService;

    public UserEditServlet() {
        userService = UserService.getInstance();
    }


    
    @Override
    public void doGet(Request request, Response response) throws IOException,ServletException {
        User user = userService.findByUsername((String) request.getSession().getAttribute("username"));
        request.setAttribute("user",user);
        request.getApplicationRequestDispatcher("/views/userEdit.html").forward(request,response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException, IOException {
        log.info("{}",request.getParameters());
        User user = new User();
        user.setUsername((String) request.getSession(false).getAttribute("username"));
        user.setRealName(request.getParameter("realName"));
        user.setAge(Integer.valueOf(request.getParameter("age")));
        userService.update(user);
        
        request.setAttribute("user",user);
        request.getApplicationRequestDispatcher("/views/user.html").forward(request, response);
    }
}
