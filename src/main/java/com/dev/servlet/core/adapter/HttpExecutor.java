package com.dev.servlet.core.adapter;

import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.application.transfer.response.IHttpResponse;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.interfaces.IHttpExecutor;
import com.dev.servlet.core.util.BeanUtil;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.presentation.controller.base.BaseRouterController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * This class is responsible for executing the HTTP request.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
public class HttpExecutor<TResponse> implements IHttpExecutor<TResponse> {

    /**
     * Executes an HTTP request with retry capability.
     *
     * @param request The request to be executed
     * @return A response object containing either the successful result or error details
     */
    @Override
    public IHttpResponse<TResponse> call(Request request) {
        EndpointParser parser;
        BaseRouterController router;
        IHttpResponse<TResponse> response;

        try {
            parser = resolveEndpoint(request);
            router = resolveController(parser);

            int maxRetries = request.retry();
            do {
                log.debug("Executing request to {} (attempt {}/{})", request.endpoint(), 1, maxRetries + 1);

                response = router.route(parser, request);
                if (response.error() == null || response.error().isEmpty()) {
                    return response;
                }

                log.error("Request to {} returned errors: {}", request.endpoint(), response.error());

                if (maxRetries <= 0) {
                    log.info("Maximum retries exhausted for endpoint {}", request.endpoint());
                    return response;
                }

                waitBeforeRetry(maxRetries);
                log.info("Retrying request to {} (attempt {}/{})", request.endpoint(), 1, maxRetries + 1);

            } while (--maxRetries > 0);

        } catch (Exception e) {
            log.error("Error processing request to {}: {}", request.endpoint(), e.getMessage(), e);
            response = handleException(e);
        }

        return response;
    }

    private static EndpointParser resolveEndpoint(Request request) throws ServiceException {
        try {
            return EndpointParser.of(request.endpoint());
        } catch (Exception e) {
            log.error("Error parsing endpoint: {}", request.endpoint(), e);
            throw new ServiceException(400, "Invalid endpoint: " + request.endpoint());
        }
    }

    private static BaseRouterController resolveController(EndpointParser parser) throws ServiceException {
        try {
            BaseRouterController controller = (BaseRouterController) BeanUtil.getResolver().getService(parser.getController());
            Objects.requireNonNull(controller);
            return controller;
        } catch (Exception e) {
            log.error("Error resolving service: {}", parser.getEndpoint(), e);
            throw new ServiceException(400, "Error resolving service endpoint: " + parser.getEndpoint());
        }
    }

    /**
     * Wait before retrying the request.
     * Example of exponential backoff: 2^attempt * 100 ms = 200 * 100 ms = 200 ms
     *
     * @param attempt the attempt number
     */
    private void waitBeforeRetry(int attempt) {
        try {
            long waitTime = (long) Math.pow(2, attempt) * 100; // Exponential backoff
            Thread.sleep(waitTime);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handles exceptions that occur during request processing.
     *
     * @param srcException The exception
     * @return The HTTP response with error details
     */
    private IHttpResponse<TResponse> handleException(Exception srcException) {
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