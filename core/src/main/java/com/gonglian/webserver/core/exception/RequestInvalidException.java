package com.gonglian.webserver.core.exception;

import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.exception.base.ServletException;

public class RequestInvalidException extends ServletException {

    private static final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    public RequestInvalidException() {
        super(httpStatus);
    }
}
