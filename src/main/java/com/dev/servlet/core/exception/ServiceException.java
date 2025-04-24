package com.dev.servlet.core.exception;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ServiceException extends Exception {
    private final String message;
    private final int code;
    public ServiceException(int code, String message) {
        this.message = message;
        this.code = code;
        log.warn(message);
    }

    public ServiceException(String message) {
        this(500, message);
        log.error(message);
    }

    public void throwError() throws ServiceException {
        throw this;
    }

    public static ServiceExceptionBuilder builder() {
        return new ServiceExceptionBuilder();
    }

    public static class ServiceExceptionBuilder {
        private String message;
        private Integer code;
        public ServiceExceptionBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ServiceExceptionBuilder code(int code) {
            this.code = code;
            return this;
        }

        public ServiceException build() {
            return new ServiceException(code, message);
        }
    }
}
