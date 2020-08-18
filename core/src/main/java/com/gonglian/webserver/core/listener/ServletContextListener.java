package com.gonglian.webserver.core.listener;

import com.gonglian.webserver.core.listener.event.ServletContextEvent;

import java.util.EventListener;

public interface ServletContextListener extends EventListener {

    /**
     * 应用初始化
     * @param sce
     */
    void contextInitialized(ServletContextEvent sce);

    /**
     * 应用关闭
     * @param sce
     */
    void contextDestroyed(ServletContextEvent sce);
}
