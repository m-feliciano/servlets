package com.dev.servlet.adapter.internal;

import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.builder.HtmlTemplate;
import com.dev.servlet.core.builder.RequestBuilder;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.adapter.IHttpExecutor;
import com.dev.servlet.core.interfaces.IRateLimiter;
import com.dev.servlet.adapter.IServletDispatcher;
import com.dev.servlet.core.util.PropertiesUtil;
import com.dev.servlet.core.util.URIUtils;
import com.dev.servlet.domain.model.enums.RequestMethod;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.MessageFormat;

@Slf4j
@NoArgsConstructor
@ApplicationScoped
public class ServletDispatcherImpl implements IServletDispatcher {
    public static final int WAIT_TIME = 600;
    @Setter
    private boolean rateLimitEnabled;
    private IHttpExecutor<?> httpExecutor;
    private IRateLimiter rateLimiter;

    @Inject
    @Named("LeakyBucketImpl")
    public void setRateLimiter(IRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Inject
    @Named("HttpExecutor")
    public void setHttpExecutor(IHttpExecutor<?> httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    @PostConstruct
    public void init() {
        rateLimitEnabled = PropertiesUtil.getProperty("rate.limit.enabled", true);
    }

    @Interceptors({LogExecutionTimeInterceptor.class})
    public void dispatch(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this.execute(servletRequest, servletResponse);
    }

    private static Request newRequest(HttpServletRequest httpServletRequest) {
        return RequestBuilder.newBuilder().httpServletRequest(httpServletRequest).complete().retry(1).build();
    }

    private void execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            if (rateLimitEnabled && !rateLimiter.acquireOrWait(WAIT_TIME)) {
                throw new ServiceException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Please try again later.");
            }
            Request request = newRequest(httpServletRequest);
            IHttpResponse<?> httpResponse = httpExecutor.call(request);
            processResponse(httpServletRequest, httpServletResponse, request, httpResponse);
        } catch (ServiceException e) {
            writeResponseError(httpServletRequest, httpServletResponse, e.getCode(), e.getMessage());
        } catch (Exception e) {
            String message = "An error occurred while processing the request. Contact the support team.";
            writeResponseError(httpServletRequest, httpServletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        }
    }

    private void setSessionAttributes(HttpSession session, UserDTO user) {
        session.setAttribute("token", user.getToken());
        session.setAttribute("user", user);
    }

    private void setRequestAttributes(HttpServletRequest httpRequest, IHttpResponse<?> response) {
        httpRequest.setAttribute("response", response);
        for (var key : httpRequest.getParameterMap().keySet()) {
            httpRequest.setAttribute(key, httpRequest.getParameter(key));
        }
    }

    private void handleResponseErrors(HttpServletRequest httpRequest, HttpServletResponse httpResponse, IHttpResponse<?> response) {
        if (response.error() != null && response.reasonText() == null) {
            String errors = String.join(", ", response.error());
            writeResponseError(httpRequest, httpResponse, response.statusCode(), errors);
        } else {
            httpResponse.setStatus(response.statusCode());
        }
    }

    private void processResponseData(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                     Request request, IHttpResponse<?> response) {
        if (RequestMethod.POST.getMethod().equals(request.getMethod()) && response.body() instanceof UserDTO userDTO) {
            setSessionAttributes(httpRequest.getSession(), userDTO);
        }
        setRequestAttributes(httpRequest, response);
        handleResponseErrors(httpRequest, httpResponse, response);
        if (request.getEndpoint() != null && request.getEndpoint().contains("logout")) {
            httpRequest.getSession().invalidate();
        }
    }

    private void processResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Request request, IHttpResponse<?> response) throws ServiceException {
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

    private void writeResponseError(HttpServletRequest httpRequest, HttpServletResponse httpResponse, int status, String message) {
        String statusMessage = URIUtils.getErrorMessage(status);
        String funnyGif = "cat_error404.gif";
        String image = MessageFormat.format("{0}/resources/assets/images/{1}", httpRequest.getContextPath(), funnyGif);
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
