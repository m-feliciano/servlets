package com.dev.servlet.exception;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class ServiceException extends Exception {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceException.class);

    private final String message;
    private final int code;

    public ServiceException(Integer code, String message) {
        this.message = message;
        this.code = code;
    }

    public ServiceException(String message) {
        this(500, message);
        LOGGER.error(message);
    }

}
