package com.dev.servlet.application.transfer.response;

import java.util.Set;

/**
 * This class is used to represent the HTTP response.
 *
 * @param <T>
 * @author marcelo.feliciano
 */
public interface IHttpResponse<T> {

    int statusCode();

    T body();

    Set<String> errors();

    String next();
}