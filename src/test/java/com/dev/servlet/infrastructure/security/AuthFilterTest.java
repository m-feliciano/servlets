package com.dev.servlet.infrastructure.security;

import com.dev.servlet.adapter.IServletDispatcher;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.core.util.PropertiesUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFilterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;
    private IServletDispatcher dispatcher;

    private AuthFilter authFilter;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
        dispatcher = mock(IServletDispatcher.class);

        authFilter = new AuthFilter();
        authFilter.setDispatcher(dispatcher);

        when(request.getSession()).thenReturn(session);
        when(request.getServletPath()).thenReturn("/product/list");
    }

    @Test
    void doFilter_WithValidToken_ShouldDispatchRequest() throws IOException {
        // Arrange
        String validToken = "valid-token";
        when(session.getAttribute("token")).thenReturn(validToken);

        try (MockedStatic<CryptoUtils> cryptoUtils = mockStatic(CryptoUtils.class)) {
            cryptoUtils.when(() -> CryptoUtils.isValidToken(validToken)).thenReturn(true);

            // Act
            authFilter.doFilter(request, response, chain);

            // Assert
            verify(dispatcher, times(1)).dispatch(request, response);
            verify(response, never()).sendRedirect(anyString());
        }
    }

    @Test
    void doFilter_WithInvalidTokenAndAuthorizedPath_ShouldDispatchRequest() throws IOException {
        // Arrange
        String invalidToken = "invalid-token";
        when(session.getAttribute("token")).thenReturn(invalidToken);

        try (MockedStatic<CryptoUtils> cryptoUtils = mockStatic(CryptoUtils.class);
             MockedStatic<EndpointParser> endpointParser = mockStatic(EndpointParser.class);
             MockedStatic<PropertiesUtil> propertiesUtil = mockStatic(PropertiesUtil.class)) {

            cryptoUtils.when(() -> CryptoUtils.isValidToken(invalidToken)).thenReturn(false);

            EndpointParser parser = mock(EndpointParser.class);
            endpointParser.when(() -> EndpointParser.of("/product/list")).thenReturn(parser);
            when(parser.getController()).thenReturn("ProductController");

            List<String> authorizedPaths = List.of("ProductController", "UserController");
            propertiesUtil.when(() -> PropertiesUtil.getProperty(eq("auth.authorized"), any())).thenReturn(authorizedPaths);

            // Act
            authFilter.init(); // Initialize the filter to set preAuthorizedPath
            authFilter.doFilter(request, response, chain);

            // Assert
            verify(dispatcher, times(1)).dispatch(request, response);
            verify(response, never()).sendRedirect(anyString());
        }
    }

    @Test
    void doFilter_WithInvalidTokenAndUnauthorizedPath_ShouldRedirectToLogin() throws IOException {
        // Arrange
        String invalidToken = "invalid-token";
        when(session.getAttribute("token")).thenReturn(invalidToken);

        try (MockedStatic<CryptoUtils> cryptoUtils = mockStatic(CryptoUtils.class);
             MockedStatic<EndpointParser> endpointParser = mockStatic(EndpointParser.class);
             MockedStatic<PropertiesUtil> propertiesUtil = mockStatic(PropertiesUtil.class)) {

            cryptoUtils.when(() -> CryptoUtils.isValidToken(invalidToken)).thenReturn(false);

            EndpointParser parser = mock(EndpointParser.class);
            endpointParser.when(() -> EndpointParser.of("/product/list")).thenReturn(parser);
            when(parser.getController()).thenReturn("ProductController");

            List<String> authorizedPaths = List.of("UserController");
            propertiesUtil.when(() -> PropertiesUtil.getProperty(eq("auth.authorized"), any())).thenReturn(authorizedPaths);
            propertiesUtil.when(() -> PropertiesUtil.getProperty("loginpage")).thenReturn("/login");

            doNothing().when(response).sendRedirect(anyString());

            // Act
            authFilter.init(); // Initialize the filter to set preAuthorizedPath
            authFilter.doFilter(request, response, chain);

            // Assert
            verify(dispatcher, never()).dispatch(request, response);
            verify(response, times(1)).sendRedirect("/login");
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    void doFilter_WithNullEndpoint_ShouldRedirectToLogin() throws IOException {
        // Arrange
        String invalidToken = "invalid-token";
        when(session.getAttribute("token")).thenReturn(invalidToken);
        when(request.getServletPath()).thenReturn(null);

        try (MockedStatic<CryptoUtils> cryptoUtils = mockStatic(CryptoUtils.class);
             MockedStatic<EndpointParser> endpointParser = mockStatic(EndpointParser.class);
             MockedStatic<PropertiesUtil> propertiesUtil = mockStatic(PropertiesUtil.class)) {

            cryptoUtils.when(() -> CryptoUtils.isValidToken(invalidToken)).thenReturn(false);

            EndpointParser parser = mock(EndpointParser.class);
            endpointParser.when(() -> EndpointParser.of(null)).thenReturn(parser);
            when(parser.getController()).thenReturn(null);
            when(parser.getEndpoint()).thenReturn(null);

            propertiesUtil.when(() -> PropertiesUtil.getProperty("loginpage")).thenReturn("/login");

            doNothing().when(response).sendRedirect(anyString());

            // Act
            authFilter.doFilter(request, response, chain);

            // Assert
            verify(dispatcher, never()).dispatch(request, response);
            verify(response, times(1)).sendRedirect("/login");
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}