package com.gonglian.webserver.core.exception;

import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.exception.base.ServletException;

public class TemplateResolverException extends ServletException {

    private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public TemplateResolverException() {
        super(status);
    }
}
