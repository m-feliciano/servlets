package com.dev.servlet.interfaces;


@FunctionalInterface
public interface IHttpExecutor {

    /**
     * Call the request
     *
     * @return {@link IHttpResponse}
     */
    IHttpResponse<Object> call();
}
