package com.gonglian.webserver.sample.web.listener;


import com.gonglian.webserver.core.listener.ServletRequestListener;
import com.gonglian.webserver.core.listener.event.ServletRequestEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MyServletRequestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        log.info("request destroy...");
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        log.info("request init...");
    }
}
