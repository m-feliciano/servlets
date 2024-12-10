package com.dev.servlet.filter;

import com.dev.servlet.filter.wrappers.SecurityRequestWrapper;
import com.dev.servlet.utils.CryptoUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This class is responsible for encrypting the password before sending it to the service layer.
 *
 * @author marcelo.feliciano
 * @since 1.4.0
 */
public class PasswordEncryptFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String passwordText = httpRequest.getParameter("password");
        String confirmationText = httpRequest.getParameter("confirmPassword");

        if (passwordText != null) {
            String password = CryptoUtils.encrypt(passwordText);
            String confirmation = confirmationText != null ? CryptoUtils.encrypt(confirmationText) : null;

            SecurityRequestWrapper wrappedRequest = new SecurityRequestWrapper(httpRequest, password, confirmation);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}