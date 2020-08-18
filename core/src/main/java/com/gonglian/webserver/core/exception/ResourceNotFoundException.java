package com.gonglian.webserver.core.exception;

import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.exception.base.ServletException;

public class ResourceNotFoundException extends ServletException {

    private static final HttpStatus status = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException() {
        super(status);
    }
}
