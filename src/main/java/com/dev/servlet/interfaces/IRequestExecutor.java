package com.dev.servlet.interfaces;

import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;

@FunctionalInterface
public interface IRequestExecutor {

    /**
     * Call the request
     *
     * @param request {@link Request}
     * @param token   {@link String} authorization
     * @return {@link Response}
     */
    Response call(Request request, String token);
}
