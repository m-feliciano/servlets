package com.dev.servlet.core.impl;

import com.dev.servlet.controller.base.BaseRouterController;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.core.IHttpExecutor;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.util.BeanUtil;
import com.dev.servlet.util.EndpointParser;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;

/**
 * This class is responsible for executing the HTTP request.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HttpExecutorImpl<J> implements IHttpExecutor<J> {

    public static <J> HttpExecutorImpl<J> newInstance() {
        return new HttpExecutorImpl<>();
    }

    /**
     * Executes the HTTP request.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
    @Override
    public IHttpResponse<J> send(Request request) {
        try {
            var parser = EndpointParser.of(request.endpoint());

            BaseRouterController routerController = resolveController(parser);
            return routerController.route(parser, request);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Resolves the controller instance based on the request path.
     *
     * @param endpoint The HTTP request
     * @return The controller instance
     */
    private BaseRouterController resolveController(EndpointParser endpoint) throws ServiceException {
        try {
            return (BaseRouterController) BeanUtil.getResolver().getService(endpoint.getService());
        } catch (Exception e) {
            throw ServiceException.badRequest("Error resolving service method for path: " + endpoint.getService());
        }
    }

    /**
     * Handles exceptions that occur during request processing.
     *
     * @param srcException The exception
     * @return The HTTP response with error details
     */
    private <U> IHttpResponse<U> handleException(Exception srcException) {
        int code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred.";

        if (srcException instanceof ServiceException exception) {
            code = exception.getCode();
            message = exception.getMessage();

        } else if (srcException.getCause() instanceof ServiceException cause) {
            code = cause.getCode();
            message = cause.getMessage();

        } else {
            log.error("An error occurred while processing the request", srcException);
        }

        return HttpResponseImpl.ofError(code, message);
    }
}