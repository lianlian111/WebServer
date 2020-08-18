package com.gonglian.webserver.core.listener;

import com.gonglian.webserver.core.listener.event.HttpSessionEvent;

import java.util.EventListener;

public interface HttpSessionListener extends EventListener {

    void sessionCreated(HttpSessionEvent hse);

    void sessionDestroyed(HttpSessionEvent hse);
}
