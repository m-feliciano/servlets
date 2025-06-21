package com.dev.servlet.application.transfer.response;

/**
 * This class is used to represent the HTTP response.
 *
 * @param <TResponse>
 * @author marcelo.feliciano
 */
public interface IHttpResponse<TResponse> {

    int statusCode();

    TResponse body();

    String error();

    String next();
}