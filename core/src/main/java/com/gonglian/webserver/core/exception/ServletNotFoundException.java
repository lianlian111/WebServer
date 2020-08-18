package com.gonglian.webserver.core.exception;

import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.exception.base.ServletException;

public class ServletNotFoundException extends ServletException {

    private static final HttpStatus status = HttpStatus.NOT_FOUND;

    public ServletNotFoundException() {
        super(status);
    }
}
