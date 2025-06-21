package com.dev.servlet.application.transfer.response;


import lombok.Builder;

/**
 * This record is used to represent the HTTP response.
 *
 * @param <TResponse> type of response
 */
@Builder(builderMethodName = "newBuilder")
public record HttpResponse<TResponse>(int statusCode, TResponse body, String next, String error) implements IHttpResponse<TResponse> {

    /**
     * Create a response with errors.
     *
     * @param status status code
     * @param error errors
     * @param <TResponse>    type of response
     * @return {@linkplain IHttpResponse} with errors
     */
    public static <TResponse> HttpResponse<TResponse> ofError(int status, String error) {
        return new HttpResponse<>(status, null, null, error);
    }

    public static <TResponse> HttpResponseBuilder<TResponse> ok() {
        return HttpResponse.<TResponse>newBuilder().statusCode(200);
    }
}

