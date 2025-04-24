package com.dev.servlet.infrastructure.security;

import com.dev.servlet.adapter.IServletDispatcher;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.core.util.PropertiesUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.dev.servlet.core.util.CryptoUtils.isValidToken;
@Slf4j
@NoArgsConstructor
public class AuthFilter implements Filter {
    public static final String LOGIN_PAGE = "loginpage";
    private List<String> preAuthorizedPath;
    private IServletDispatcher dispatcher;

    @Inject
    @Named("ServletDispatcherImpl")
    public void setDispatcher(IServletDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostConstruct
    public void init() {
        List<String> defaultAuthorized = List.of("LoginController,UserController");
        preAuthorizedPath = PropertiesUtil.getProperty("auth.authorized", defaultAuthorized);
        log.info("Auth filter initialized with pre-authorized paths: {}", preAuthorizedPath);
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token = (String) request.getSession().getAttribute("token");
        if (!isValidToken(token) && !isAuthorizedRequest(request)) {
            log.warn("Unauthorized access to the service: {}, redirecting to login page", request.getRequestURI());
            redirectToLogin(response);
            return;
        }
        log.debug("Access to the endpoint: {}, authorized", request.getRequestURI());
        dispatcher.dispatch(request, response);
    }

    private void redirectToLogin(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.sendRedirect(PropertiesUtil.getProperty(LOGIN_PAGE));
    }

    private boolean isAuthorizedRequest(HttpServletRequest request) {
        var parser = EndpointParser.of(request.getServletPath());
        String controller = parser.getController();
        String endpoint = parser.getEndpoint();
        if (endpoint == null && controller == null) {
            return false;
        }
        return controller != null && preAuthorizedPath.contains(controller);
    }
}