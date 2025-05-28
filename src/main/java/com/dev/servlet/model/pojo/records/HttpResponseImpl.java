package com.dev.servlet.model.pojo.records;

import com.dev.servlet.adapter.IHttpResponse;
import lombok.Builder;

import java.util.Set;

/**
 * This record is used to represent the HTTP response.
 *
 * @param <T> type of response
 */
@Builder(builderMethodName = "newBuilder")
public record HttpResponseImpl<T>(int statusCode, T body, String next, Set<String> errors) implements IHttpResponse<T> {

    /**
     * Create a response with errors.
     *
     * @param status status code
     * @param errors errors
     * @param <U>    type of response
     * @return {@linkplain HttpResponseImpl}
     */
    public static <U> HttpResponseImpl<U> ofError(int status, Set<String> errors) {
        return new HttpResponseImpl<>(status, null, null, errors);
    }

    /**
     * @see HttpResponseImpl#ofError(int, Set)
     */
    public static <U> HttpResponseImpl<U> ofError(int status, String error) {
        return ofError(status, Set.of(error));
    }

    public static <U> HttpResponseImpl.HttpResponseImplBuilder<U> ok() {
        return HttpResponseImpl.<U>newBuilder().statusCode(200);
    }
}