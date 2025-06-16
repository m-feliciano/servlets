package com.dev.servlet.core.interfaces;

import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.IHttpResponse;

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
