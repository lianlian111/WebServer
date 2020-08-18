package com.gonglian.webserver.core.listener;

import com.gonglian.webserver.core.listener.event.ServletRequestEvent;

import java.util.EventListener;

public interface ServletRequestListener extends EventListener {

    void requestInitialized(ServletRequestEvent sre);

    void requestDestroyed(ServletRequestEvent sre);
}
