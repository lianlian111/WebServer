package com.gonglian.webserver.core.exception;

import com.gonglian.webserver.core.enumeration.HttpStatus;
import com.gonglian.webserver.core.exception.base.ServletException;

public class ServerErrorException extends ServletException {

    private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    public ServerErrorException() {
        super(status);
    }
}
