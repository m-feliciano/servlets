package com.dev.servlet.infrastructure.security;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.infrastructure.security.wrapper.SecurityRequestWrapper;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class PasswordEncryptFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
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
