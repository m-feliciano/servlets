package com.dev.servlet.auth;

import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.core.IServletDispatcher;
import com.dev.servlet.util.CryptoUtils;
import com.dev.servlet.util.EndpointParser;
import com.dev.servlet.util.PropertiesUtil;
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

/**
 * Authentication filter to check user authorization for accessing services.
 *
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor
public class Auth implements Filter {

    public static final String LOGINPAGE = "loginpage";
    public static final String USER = "user";

    private List<String> preAuthorizedPath;
    private IServletDispatcher dispatcher;

    @Inject
    @Named("ServletDispatcherImpl") // No need to specify the name if the class has only one implementation
    public void setDispatcher(IServletDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostConstruct
    public void init() {
        preAuthorizedPath = PropertiesUtil.getProperty("auth.authorized", List.of("/login,/user"));
        log.info("Auth filter initialized with pre-authorized paths: {}", preAuthorizedPath);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        UserDTO user = (UserDTO) request.getSession().getAttribute(USER);
        String token = user != null ? user.getToken() : null;

        if (isAuthorizedRequest(request, token)) {
            log.debug("Access to the service: {}, authorized", request.getRequestURI());
            dispatcher.dispatch(request, response);
        } else {
            log.warn("Unauthorized access to the service: {}, redirecting to login page", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect(PropertiesUtil.getProperty(LOGINPAGE));
        }
    }

    /**
     * Checks if the token is valid.
     *
     * @param token the session token
     * @return true if the token is valid, false otherwise
     */
    private boolean isValidToken(String token) {
        return token != null && CryptoUtils.verifyToken(token);
    }

    /**
     * Checks if the user is authorized to access the action.
     *
     * @param request the HTTP request
     * @return true if the action is authorized, false otherwise
     */
    private boolean isAuthorizedRequest(HttpServletRequest request, String token) {
        if (isValidToken(token)) return true;

        var parser = EndpointParser.of(request.getServletPath());
        String service = parser.getService();
        String serviceName = parser.getServiceName();

        if (serviceName == null && service == null) {
            return false;
        }

        return service != null && preAuthorizedPath.contains(service);
    }
}