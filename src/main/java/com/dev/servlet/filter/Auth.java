package com.dev.servlet.filter;

import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.interfaces.IServletDispatcher;
import com.dev.servlet.utils.CryptoUtils;
import com.dev.servlet.utils.PropertiesUtil;
import com.dev.servlet.utils.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Auth implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth.class);

    private static final List<String> AUTHORIZED_PATH =
            PropertiesUtil.getProperty("auth.authorized", List.of("login, registerUser"));

    @Inject
    @Named("ServletDispatch") // No need to specify the name if the class has only one implementation
    private IServletDispatcher dispatcher;

    public Auth() {
        // Empty constructor
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        UserDTO user = (UserDTO) request.getSession().getAttribute("user");
        String token = user != null ? user.getToken() : null;

        if (isAuthorizedAction(request) || isValidToken(token)) {
            dispatcher.dispatch(request, response);
        } else {
            LOGGER.warn("Unauthorized access to the service: {}, redirecting to login page", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect(PropertiesUtil.getProperty("loginpage"));
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
    private boolean isAuthorizedAction(HttpServletRequest request) {
        String action = URIUtils.getServiceName(request);
        String service = URIUtils.getServicePath(request);

        if (action == null && service == null) {
            return false;
        }

        if (service != null && AUTHORIZED_PATH.contains(service)) {
            return true;
        }

        return action != null && AUTHORIZED_PATH.contains(action);
    }
}