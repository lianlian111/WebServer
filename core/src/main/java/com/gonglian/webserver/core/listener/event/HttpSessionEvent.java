package com.gonglian.webserver.core.listener.event;

import com.gonglian.webserver.core.session.HttpSession;

import java.util.EventObject;

public class HttpSessionEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public HttpSessionEvent(HttpSession source) {
        super(source);
    }
}
