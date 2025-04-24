package com.dev.servlet.domain.transfer.response;

import lombok.Builder;

import javax.servlet.http.HttpServletResponse;

@Builder(builderMethodName = "newBuilder")
public record HttpResponse<T>(
        int statusCode,
        T body,
        String next,
        String error,
        String reasonText) implements IHttpResponse<T> {

    public static <T> HttpResponse<T> error(int status, String error) {
        return new HttpResponse<>(status, null, null, error, null);
    }

    public static <T> HttpResponseBuilder<T> next(String next) {
        return HttpResponse.<T>newBuilder()
                .statusCode(HttpServletResponse.SC_OK)
                .next(next);
    }

    public static <T> HttpResponseBuilder<T> ok(T body) {
        return HttpResponse.<T>newBuilder()
                .statusCode(HttpServletResponse.SC_OK)
                .body(body);
    }
}
