package com.dev.servlet.core.adapter;

import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.application.dto.response.HttpResponse;
import com.dev.servlet.application.dto.response.IHttpResponse;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.interfaces.IHttpExecutor;
import com.dev.servlet.core.util.BeanUtil;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.presentation.controller.base.BaseRouterController;
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

            BaseRouterController router = resolveController(parser);
            return router.route(parser, request);
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
            return (BaseRouterController) BeanUtil.getResolver().getService(endpoint.getController());
        } catch (Exception e) {
            String message = "Error resolving service method for path: " + endpoint.getController();
            log.error(message, e);
            throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, message);
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

        return HttpResponse.ofError(code, message);
    }
}