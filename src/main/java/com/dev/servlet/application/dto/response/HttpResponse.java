package com.dev.servlet.application.dto.response;


import lombok.Builder;

import java.util.Set;

/**
 * This record is used to represent the HTTP response.
 *
 * @param <T> type of response
 */
@Builder(builderMethodName = "newBuilder")
public record HttpResponse<T>(int statusCode, T body, String next, Set<String> errors) implements IHttpResponse<T> {

    /**
     * Create a response with errors.
     *
     * @param status status code
     * @param errors errors
     * @param <U>    type of response
     * @return {@linkplain IHttpResponse} with errors
     */
    public static <U> HttpResponse<U> ofError(int status, Set<String> errors) {
        return new HttpResponse<>(status, null, null, errors);
    }

    /**
     * @see HttpResponse#ofError(int, Set)
     */
    public static <U> HttpResponse<U> ofError(int status, String error) {
        return ofError(status, Set.of(error));
    }

    public static <U> HttpResponseBuilder<U> ok() {
        return HttpResponse.<U>newBuilder().statusCode(200);
    }
}

