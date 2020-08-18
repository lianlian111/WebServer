package com.gonglian.webserver.core.context;

import com.gonglian.webserver.core.exception.handler.ExceptionHandler;
import com.gonglian.webserver.core.resource.ResourceHandler;

/**
 * 静态的servletContext, 保持servletContext能在项目启动时就被初始化
 */
public class WebApplication {

    public static Context context;
    public static ExceptionHandler exceptionHandler;
    public static ResourceHandler resourceHandler;

    static{
        try {
            context = new Context();
            exceptionHandler = new ExceptionHandler();
            resourceHandler = new ResourceHandler(exceptionHandler);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static Context getContext(){
        return context;
    }

    public static ExceptionHandler getExceptionHandler(){
        return exceptionHandler;
    }

    public static ResourceHandler getResourceHandler(){
        return resourceHandler;
    }
}
