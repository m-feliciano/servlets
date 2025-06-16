package com.dev.servlet.infrastructure.security.wrapper;

import com.dev.servlet.infrastructure.security.wrapper.SecurityRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityRequestWrapperTest {

    private SecurityRequestWrapper wrapper;
    private final String encryptedPassword = "encryptedPassword";

    @BeforeEach
    void setUp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("password", new String[]{"originalPassword"});
        parameterMap.put("confirmPassword", new String[]{"originalConfirmPassword"});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("password")).thenReturn("originalPassword");
        when(request.getParameter("confirmPassword")).thenReturn("originalConfirmPassword");

        wrapper = new SecurityRequestWrapper(request, encryptedPassword, encryptedPassword);
    }

    @Test
    @DisplayName("It should return the encrypted password.")
    void testGetParameter() {
        assertEquals(encryptedPassword, wrapper.getParameter("password"));
        assertEquals(encryptedPassword, wrapper.getParameter("confirmPassword"));
    }

    @Test
    @DisplayName("It should return an array with the encrypted password.")
    void testGetParameterValues() {
        assertEquals(encryptedPassword, wrapper.getParameterValues("password")[0]);
        assertEquals(encryptedPassword, wrapper.getParameterValues("confirmPassword")[0]);
    }

    @Test
    @DisplayName("It should return a map with the encrypted password.")
    void testGetParameterMap() {
        Map<String, String[]> parameterMap = wrapper.getParameterMap();
        assertEquals(encryptedPassword, parameterMap.get("password")[0]);
        assertEquals(encryptedPassword, parameterMap.get("confirmPassword")[0]);
    }
}