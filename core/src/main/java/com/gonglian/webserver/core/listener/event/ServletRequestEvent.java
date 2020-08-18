package com.gonglian.webserver.core.listener.event;

import java.util.EventObject;

public class ServletRequestEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServletRequestEvent(Object source) {
        super(source);
    }
}
