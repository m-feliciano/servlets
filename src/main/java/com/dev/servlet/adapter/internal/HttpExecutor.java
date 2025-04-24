package com.dev.servlet.adapter.internal;

import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.adapter.IHttpExecutor;
import com.dev.servlet.core.util.BeanUtil;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.controller.base.BaseRouterController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class HttpExecutor<TResponse> implements IHttpExecutor<TResponse> {

    @Override
    public IHttpResponse<TResponse> call(Request request) {
        EndpointParser parser;
        BaseRouterController router;
        IHttpResponse<TResponse> response;
        try {
            parser = resolveEndpoint(request);
            router = resolveController(parser);
            int maxRetries = request.getRetry();
            do {
                log.debug("Executing request to {} (attempt {}/{})", request.getEndpoint(), 1, maxRetries + 1);
                response = router.route(parser, request);
                if (response.statusCode() >= 200 && response.statusCode() < 400) {
                    return response;
                }
                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    log.warn("Client error for request to {}: {}", request.getEndpoint(), response.error());
                    return response;
                }
                log.error("Request to {} returned errors: {}", request.getEndpoint(), response.error());
                if (maxRetries <= 0) {
                    log.info("Maximum retries exhausted for endpoint {}", request.getEndpoint());
                    return response;
                }
                waitBeforeRetry(maxRetries);
                log.info("Retrying request to {} (attempt {}/{})", request.getEndpoint(), 1, maxRetries + 1);
            } while (--maxRetries > 0);
        } catch (Exception e) {
            log.error("Error processing request to {}: {}", request.getEndpoint(), e.getMessage(), e);
            response = handleException(e);
        }
        return response;
    }

    private static EndpointParser resolveEndpoint(Request request) throws ServiceException {
        try {
            return EndpointParser.of(request.getEndpoint());
        } catch (Exception e) {
            log.error("Error parsing endpoint: {}", request.getEndpoint(), e);
            throw new ServiceException(400, "Invalid endpoint: " + request.getEndpoint());
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

    private void waitBeforeRetry(int attempt) {
        try {
            long waitTime = (long) Math.pow(2, attempt) * 100;
            Thread.sleep(waitTime);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

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
        return HttpResponse.error(code, message);
    }
}
