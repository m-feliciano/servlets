package com.dev.servlet.providers;

import com.dev.servlet.builders.HtmlTemplate;
import com.dev.servlet.builders.RequestFactory;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.interfaces.IHttpExecutor;
import com.dev.servlet.interfaces.IHttpResponse;
import com.dev.servlet.interfaces.IRateLimiter;
import com.dev.servlet.interfaces.IServletDispatcher;
import com.dev.servlet.listeners.LogExecutionTimeInterceptor;
import com.dev.servlet.pojo.records.Query;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.PropertiesUtil;
import com.dev.servlet.utils.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.MessageFormat;

/**
 * This class is responsible for dispatching the request to the appropriate servlet.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
@Singleton
@Named("ServletDispatch")
public class ServletDispatcher implements IServletDispatcher {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServletDispatcher.class);
    public static final int WAIT_TIME = 600; // 600ms

    private final IRateLimiter rateLimiter = new LeakyBucket();
    private boolean rateLimitEnabled;

    public ServletDispatcher() {
        // Empty constructor
    }

    @PostConstruct
    public void init() {
        rateLimitEnabled = PropertiesUtil.getProperty("rate.limit.enabled", true);
    }

    /**
     * Dispatch the request to the appropriate servlet.
     *
     * @param servletRequest  {@link HttpServletRequest}
     * @param servletResponse {@link HttpServletResponse}
     */
    @Interceptors({LogExecutionTimeInterceptor.class})
    public void dispatch(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this.execute(servletRequest, servletResponse);
    }

    /**
     * Execute the request and return the next path.
     *
     * @param httpRequest  {@link HttpServletRequest}
     * @param httpResponse {@link HttpServletResponse}
     * @author marcelo.feliciano
     */
    private void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            if (rateLimitEnabled && !rateLimiter.acquireOrWait(WAIT_TIME)) {
                throw new ServiceException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Please try again later.");
            }

            Request request = RequestFactory.factory()
                    .httpServletRequest(httpRequest).complete()
                    .retry(2)
                    .create();

            IHttpExecutor httpExecutor = new LocalHttpExecutor(request);
            IHttpResponse<?> response = this.executeWithRetry(httpExecutor, request);

            processResponse(httpRequest, httpResponse, request, response);

        } catch (ServiceException e) {
            writeResponseError(httpRequest, httpResponse, e.getCode(), e.getMessage());

        } catch (Exception e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            writeResponseError(httpRequest, httpResponse, status, message);
        }
    }

    /**
     * Execute the request with retry.
     *
     * @param httpExecutor {@link IHttpExecutor} HTTP executor
     * @param request      {@link Request} Internal request
     * @return
     */
    private IHttpResponse<?> executeWithRetry(IHttpExecutor httpExecutor, Request request) {
        IHttpResponse<?> response = null;
        int retry = request.getRetry();
        do {
            try {
                response = httpExecutor.call();

                if (response.getErrors() == null) {
                    break;
                }
            } catch (Exception ignored) {
                // Retry
            }

            LOGGER.warn("Retrying request: {}", request.getEndpoint());
            retry--;
        } while (retry >= 0);

        return response;
    }

    /**
     * Set the session attributes.
     *
     * @param session {@link HttpSession}
     * @param user    {@link UserDTO} User data transfer object
     */
    private void setSessionAttributes(HttpSession session, UserDTO user) {
        session.setAttribute("token", user.getToken());
        session.setAttribute("user", user);
    }

    /**
     * Set the request attributes.
     *
     * @param httpRequest {@link HttpServletRequest}
     * @param response    {@link IHttpResponse} Internal response
     * @param query       {@link Query} Query object
     */
    private void setRequestAttributes(HttpServletRequest httpRequest, IHttpResponse<?> response, Query query) {
        httpRequest.setAttribute("response", response);
        httpRequest.setAttribute("query", query);

        for (var key : httpRequest.getParameterMap().keySet()) {
            httpRequest.setAttribute(key, httpRequest.getParameter(key));
        }
    }

    /**
     * Handle the response errors.
     *
     * @param httpRequest  {@link HttpServletRequest}
     * @param httpResponse {@link HttpServletResponse}
     * @param response     {@link IHttpResponse} Internal response
     */
    private void handleResponseErrors(HttpServletRequest httpRequest, HttpServletResponse httpResponse, IHttpResponse<?> response) {
        if (response.getErrors() != null) {
            String errors = String.join(", ", response.getErrors());
            writeResponseError(httpRequest, httpResponse, response.getStatus(), errors);
        } else {
            httpResponse.setStatus(response.getStatus());
        }
    }


    /**
     * Process the response data and set request attributes.
     *
     * @param httpRequest  {@link HttpServletRequest}
     * @param httpResponse {@link HttpServletResponse}
     * @param request      {@link Request}
     * @param response     {@link IHttpResponse}
     * @author marcelo.feliciano
     */
    private void processResponseData(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Request request, IHttpResponse<?> response) {

        if (request.getToken() == null && response.getResponse() instanceof UserDTO user) {
            this.setSessionAttributes(httpRequest.getSession(), user);
        } else {
            this.setRequestAttributes(httpRequest, response, request.getQuery());
        }

        this.handleResponseErrors(httpRequest, httpResponse, response);

        if (request.getEndpoint().contains("logout")) {
            httpRequest.getSession().invalidate();
        }
    }

    /**
     * Process the response data and redirect to the next path.
     *
     * @param httpRequest  {@link HttpServletRequest} HTTP request
     * @param httpResponse {@link HttpServletResponse} HTTP response
     * @param request      {@link Request} Internal request
     * @param response     {@link IHttpResponse} Internal response
     * @throws ServiceException if an error occurs
     * @author marcelo.feliciano
     */
    private void processResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                 Request request, IHttpResponse<?> response) throws ServiceException {

        processResponseData(httpRequest, httpResponse, request, response);

        if (response.getNext() == null) return;

        String[] path = response.getNext().split(":");
        if (path.length != 2) {
            throw new ServiceException("Cannot parse URL: " + response.getNext());
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
     * @param httpResponse {@link HttpServletResponse}
     * @param status       the HTTP status code
     * @param message      the error message
     * @author marcelo.feliciano
     */
    private void writeResponseError(HttpServletRequest httpRequest, HttpServletResponse httpResponse, int status, String message) {
        String statusMessage = URIUtils.getErrorMessage(status);
        String funnyGif = "cat_error404.gif"; // cat GIF!
        String image = MessageFormat.format("{0}/assets/images/{1}", httpRequest.getContextPath(), funnyGif);

        String htmlErrorPage = HtmlTemplate.builder()
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
            LOGGER.error("Error writing response: {}", cause);
        }
    }
}