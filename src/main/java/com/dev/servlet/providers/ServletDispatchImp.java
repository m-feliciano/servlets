package com.dev.servlet.providers;

import com.dev.servlet.builders.HtmlTemplate;
import com.dev.servlet.builders.RequestBuilder;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.interfaces.IRateLimiter;
import com.dev.servlet.interfaces.IRequestExecutor;
import com.dev.servlet.interfaces.IServletDispatcher;
import com.dev.servlet.listeners.LogExecutionTimeInterceptor;
import com.dev.servlet.pojo.records.KeyPair;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
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
 * This class is used to orchestrate the relationship between the business's and servlet's logic.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
@Singleton
@Named("ServletDispatch")
public class ServletDispatchImp implements IServletDispatcher {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServletDispatchImp.class);

    public static final String TOKEN = "token";
    public static final String USER = "user";
    public static final String QUERY = "query";
    public static final String LOGOUT = "logout";
    public static final int WAIT_TIME = 600; // 600ms

    private boolean rateLimitEnabled;

    public ServletDispatchImp() {
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
            if (rateLimitEnabled) {
                IRateLimiter rateLimiter = new LeakyBucket() {
                };

                if (!rateLimiter.acquireOrWait(WAIT_TIME)) {
                    throw new ServiceException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Please try again later.");
                }
            }

            HttpSession httpSession = httpRequest.getSession();
            String token = (String) httpSession.getAttribute(TOKEN);

            Request request = RequestBuilder.builder().httpServletRequest(httpRequest).build();

            Response response = executor().call(request, token);
            processResponse(httpRequest, httpResponse, request, response);

        } catch (Exception e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            if (e instanceof ServiceException serviceException) {
                status = serviceException.getCode();
                message = serviceException.getMessage();
            }

            writeResponseError(httpRequest, httpResponse, status, message);
        }
    }

    /**
     * Get a new instance of the {@link IRequestExecutor}.
     *
     * @return {@link RequestExecutorImp}
     * @author marcelo.feliciano
     */
    private IRequestExecutor executor() {
        // I chose to not use the injector to create the instance of the RequestExecutorImp class
        // because it is a 'dummy' class and does not have any dependencies.
        return (request, token) -> {
            var implementation = new RequestExecutorImp();
            return implementation.call(request, token);
        };
    }

    /**
     * Process the response data and set request attributes.
     *
     * @param httpRequest  {@link HttpServletRequest}
     * @param httpResponse {@link HttpServletResponse}
     * @param request      {@link Request}
     * @param response     {@link Response}
     * @author marcelo.feliciano
     */
    private void processResponseData(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                     Request request, Response response) {

        HttpSession session = httpRequest.getSession();

        Response.Data responseData = response.getResponseData();
        if (responseData != null) {
            for (KeyPair keyPair : responseData.get()) {
                httpRequest.setAttribute(keyPair.getKey(), keyPair.getValue());
            }

            Object token = responseData.get(TOKEN);
            if (token != null) {
                session.setAttribute(TOKEN, token);
            }

            Object user = responseData.get(USER);
            if (user != null) {
                session.setAttribute(USER, user);
            }
        }

        httpRequest.setAttribute(QUERY, request.getQuery());

        // Set the request parameters as attributes
        for (var key : httpRequest.getParameterMap().keySet()) {
            httpRequest.setAttribute(key, httpRequest.getParameter(key));
        }

        if (response.getErrors() != null) {
            writeResponseError(httpRequest, httpResponse, response.getStatus(), response.getErrorMessage());
        } else {
            httpResponse.setStatus(response.getStatus());
        }

        if (request.getEndpoint().contains(LOGOUT)) {
            session.invalidate();
        }
    }

    /**
     * Process the response data and redirect to the next path.
     *
     * @param httpRequest  {@link HttpServletRequest} HTTP request
     * @param httpResponse {@link HttpServletResponse} HTTP response
     * @param request      {@link Request} Internal request
     * @param response     {@link Response} Internal response
     * @throws ServiceException if an error occurs
     * @author marcelo.feliciano
     */
    private void processResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                 Request request, Response response) throws ServiceException {

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