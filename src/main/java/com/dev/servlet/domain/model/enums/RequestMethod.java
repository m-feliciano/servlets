package com.dev.servlet.domain.model.enums;
import lombok.Getter;

public enum RequestMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    OPTIONS("OPTIONS");
    @Getter
    private final String method;
    RequestMethod(String method) {
        this.method = method;
    }

    public static RequestMethod fromString(String method) {
        for (RequestMethod requestMethod : RequestMethod.values()) {
            if (requestMethod.getMethod().equalsIgnoreCase(method)) {
                return requestMethod;
            }
        }
        throw new IllegalArgumentException("No enum constant " + RequestMethod.class.getCanonicalName() + "." + method);
    }
}
