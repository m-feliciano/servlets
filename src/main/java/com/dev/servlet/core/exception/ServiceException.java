package com.dev.servlet.core.exception;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ServiceException extends Exception {

    private final String message;
    private final int code;

    public ServiceException(Integer code, String message) {
        this.message = message;
        this.code = code;
        log.warn(message);
    }

    public ServiceException(String message) {
        this(500, message);
        log.error(message);
    }

}

