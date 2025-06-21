package com.dev.servlet.core.interfaces;

import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.IHttpResponse;

/**
 * This interface is responsible for executing the HTTP request.
 *
 * @param <TResponse> the type of the response body
 * @since 1.0.0
 */
@FunctionalInterface
public interface IHttpExecutor<TResponse> {

    /**
     * Sends the request to the server.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
    IHttpResponse<TResponse> call(Request request);
}
