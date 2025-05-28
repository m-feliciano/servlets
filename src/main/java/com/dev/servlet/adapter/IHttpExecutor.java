package com.dev.servlet.adapter;


import com.dev.servlet.model.pojo.records.Request;

/**
 * This interface is responsible for executing the HTTP request.
 *
 * @param <U> the type of the response body
 * @since 1.0.0
 */
@FunctionalInterface
public interface IHttpExecutor<U> {

    /**
     * Sends the request to the server.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
    IHttpResponse<U> send(Request request);
}
