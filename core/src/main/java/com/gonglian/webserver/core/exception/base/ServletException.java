package com.gonglian.webserver.core.exception.base;

import com.gonglian.webserver.core.enumeration.HttpStatus;
import lombok.Getter;

/**
 * 根异常
 */
@Getter
public class ServletException extends Exception {
    private HttpStatus httpStatus;

    public ServletException(HttpStatus httpStatus){
        this.httpStatus = httpStatus;
    }
}
