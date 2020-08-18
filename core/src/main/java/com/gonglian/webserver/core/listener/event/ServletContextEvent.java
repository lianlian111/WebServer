package com.gonglian.webserver.core.listener.event;

import com.gonglian.webserver.core.context.Context;

import java.util.EventObject;

public class ServletContextEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServletContextEvent(Context source) {
        super(source);
    }
}
