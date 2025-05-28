package com.dev.servlet.core.impl;

import com.dev.servlet.core.IHttpExecutor;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.IRateLimiter;
import com.dev.servlet.core.IServletDispatcher;
import com.dev.servlet.core.builder.HtmlTemplate;
import com.dev.servlet.core.builder.RequestBuilder;
import com.dev.servlet.core.interceptor.LogExecutionTimeInterceptor;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.util.PropertiesUtil;
import com.dev.servlet.util.URIUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Set;

/**
 * This class is responsible for dispatching the request to the appropriate servlet.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
@Slf4j
@Singleton
public class ServletDispatcherImpl implements IServletDispatcher {

    public static final int WAIT_TIME = 600; // 600ms

    private IRateLimiter rateLimiter;

    @Setter
    private boolean rateLimitEnabled;

    @Inject
    @Named("LeakyBucketImpl")
    public void setRateLimiter(IRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public ServletDispatcherImpl() {
        // Empty constructor
    }

    @PostConstruct
    public void init() {
        rateLimitEnabled = PropertiesUtil.getProperty("rate.limit.enabled", true);
    }

    /**
     * Dispatch the request to the appropriate servlet.
     *
     * @param servletRequest  {@linkplain HttpServletRequest}
     * @param servletResponse {@linkplain HttpServletResponse}
     */
    @Interceptors({LogExecutionTimeInterceptor.class})
    public void dispatch(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this.execute(servletRequest, servletResponse);
    }

    /**
     * Execute the request and return the next path.
     *
     * @param httpServletRequest  {@linkplain HttpServletRequest}
     * @param httpServletResponse {@linkplain HttpServletResponse}
     * @author marcelo.feliciano
     */
    private void execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            if (rateLimitEnabled && !rateLimiter.acquireOrWait(WAIT_TIME)) {
                throw new ServiceException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Please try again later.");
            }

            Request request = newRequest(httpServletRequest);

            IHttpExecutor<?> httpExecutor = HttpExecutorImpl.newInstance();
            IHttpResponse<?> httpResponse = executeRequest(httpExecutor, request);

            processResponse(httpServletRequest, httpServletResponse, request, httpResponse);

        } catch (ServiceException e) {
            writeResponseError(httpServletRequest, httpServletResponse, e.getCode(), e.getMessage());

        } catch (Exception e) {
            String message = "An error occurred while processing the request. Contact the support team.";

            writeResponseError(httpServletRequest, httpServletResponse,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        }
    }

    /**
     * Build the request object.
     *
     * @param httpServletRequest {@linkplain HttpServletRequest}
     * @return {@linkplain Request} Internal request
     */
    private static Request newRequest(HttpServletRequest httpServletRequest) {
        return RequestBuilder.newBuilder()
                .httpServletRequest(httpServletRequest).complete()
                .retry(1)
                .build();
    }

    /**
     * Execute the request.
     *
     * @param httpExecutor {@linkplain IHttpExecutor} HTTP executor
     * @param request      {@linkplain Request} Internal request
     * @return {@linkplain IHttpResponse} Internal response
     */
    private IHttpResponse<?> executeRequest(IHttpExecutor<?> httpExecutor, Request request) {
        IHttpResponse<?> response;
        if (request.retry() > 0) {
            response = this.sendWithRetry(httpExecutor, request);
        } else {
            response = httpExecutor.send(request);
        }
        return response;
    }

    /**
     * Execute the request with retry.
     *
     * @param httpExecutor {@linkplain IHttpExecutor} HTTP executor
     * @param request      {@linkplain Request} Internal request
     * @return {@linkplain IHttpResponse} Internal response
     */
    public  <U> IHttpResponse<U> sendWithRetry(IHttpExecutor<U> httpExecutor, Request request) {
        int attempt = -1;
        IHttpResponse<U> response;

        do {
            attempt++;

            response = httpExecutor.send(request);

            if (response.errors() == null || response.errors().isEmpty()) {
                break;
            } else {
                logErrors(response.errors());
            }

            if (request.retry() > attempt) {
                waitBeforeRetry(attempt);
                log.info("Retrying request {} attempt={}", request.endpoint(), attempt + 1);
            }

        } while (attempt < request.retry());

        return response;
    }

    /**
     * Log the errors.
     *
     * @param errors {@linkplain Set} of errors
     */
    private void logErrors(Set<String> errors) {
        String messages = String.join(", ", errors);
        log.error("Error executing request: {}", messages);
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
     * Set the session attributes.
     *
     * @param session {@linkplain HttpSession}
     * @param user    {@linkplain UserDTO} User data transfer object
     */
    private void setSessionAttributes(HttpSession session, UserDTO user) {
        session.setAttribute("token", user.getToken());
        session.setAttribute("user", user);
    }

    /**
     * Set the request attributes.
     *
     * @param httpRequest {@linkplain HttpServletRequest}
     * @param response    {@linkplain IHttpResponse} Internal response
     */
    private void setRequestAttributes(HttpServletRequest httpRequest, IHttpResponse<?> response) {
        httpRequest.setAttribute("response", response);

        for (var key : httpRequest.getParameterMap().keySet()) {
            httpRequest.setAttribute(key, httpRequest.getParameter(key));
        }
    }

    /**
     * Handle the response errors.
     *
     * @param httpRequest  {@linkplain HttpServletRequest}
     * @param httpResponse {@linkplain HttpServletResponse}
     * @param response     {@linkplain IHttpResponse} Internal response
     */
    private void handleResponseErrors(HttpServletRequest httpRequest, HttpServletResponse httpResponse, IHttpResponse<?> response) {
        if (response.errors() != null) {
            String errors = String.join(", ", response.errors());
            writeResponseError(httpRequest, httpResponse, response.statusCode(), errors);
        } else {
            httpResponse.setStatus(response.statusCode());
        }
    }


    /**
     * Process the response data and set request attributes.
     *
     * @param httpRequest  {@linkplain HttpServletRequest}
     * @param httpResponse {@linkplain HttpServletResponse}
     * @param request      {@linkplain Request}
     * @param response     {@linkplain IHttpResponse}
     * @author marcelo.feliciano
     */
    private void processResponseData(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Request request, IHttpResponse<?> response) {

        if (request.token() == null && response.body() instanceof UserDTO user) {
            this.setSessionAttributes(httpRequest.getSession(), user);
        } else {
            this.setRequestAttributes(httpRequest, response);
        }

        this.handleResponseErrors(httpRequest, httpResponse, response);

        if (request.endpoint() != null && request.endpoint().contains("logout")) {
            httpRequest.getSession().invalidate();
        }
    }

    /**
     * Process the response data and redirect to the next path.
     *
     * @param httpRequest  {@linkplain HttpServletRequest} HTTP request
     * @param httpResponse {@linkplain HttpServletResponse} HTTP response
     * @param request      {@linkplain Request} Internal request
     * @param response     {@linkplain IHttpResponse} Internal response
     * @throws ServiceException if an error occurs
     * @author marcelo.feliciano
     */
    private void processResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                 Request request, IHttpResponse<?> response) throws ServiceException {

        processResponseData(httpRequest, httpResponse, request, response);

        if (response.next() == null) return;

        String[] path = response.next().split(":");
        if (path.length != 2) {
            throw new ServiceException("Cannot parse URL: " + response.next());
        }

        String pathAction = path[0];
        String pathUrl = path[1];

        try {
            if ("forward".equalsIgnoreCase(pathAction)) {
                httpRequest.getRequestDispatcher("/WEB-INF/view/" + pathUrl).forward(httpRequest, httpResponse);
            } else {
                httpResponse.sendRedirect(pathUrl);
            }
        } catch (Exception e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new ServiceException("Error processing httpRequest: " + message);
        }
    }

    /**
     * Send an error response to the client in HTML format.
     *
     * @param httpResponse {@linkplain HttpServletResponse}
     * @param status       the HTTP status code
     * @param message      the error message
     * @author marcelo.feliciano
     */
    private void writeResponseError(HttpServletRequest httpRequest, HttpServletResponse httpResponse, int status, String message) {
        String statusMessage = URIUtils.getErrorMessage(status);
        String funnyGif = "cat_error404.gif"; // cat GIF!
        String image = MessageFormat.format("{0}/assets/images/{1}", httpRequest.getContextPath(), funnyGif);

        String htmlErrorPage = HtmlTemplate.newBuilder()
                .error(status)
                .subTitle(statusMessage)
                .message(message)
                .image(image)
                .build();

        httpResponse.setStatus(status);
        httpResponse.setContentType("text/html");
        httpResponse.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = httpResponse.getWriter()) {
            writer.write(htmlErrorPage);
            writer.flush();
        } catch (Exception e) {
            String cause = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            log.error("Error writing response: {}", cause);
        }
    }
}