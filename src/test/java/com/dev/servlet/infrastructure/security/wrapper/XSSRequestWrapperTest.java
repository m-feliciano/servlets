package com.dev.servlet.infrastructure.security.wrapper;

import com.dev.servlet.infrastructure.security.wrapper.XSSRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XSSRequestWrapperTest {

    public static final String SCRIPT_ALERT_XSS_SCRIPT = "<script>alert('xss')</script>";
    public static final String COMPLEX_XSS_SCRIPT = "<img src=x onerror=alert('xss')>";
    public static final String HACKER_EMAIL =  "<script type=\"text/javascript\">" +
            "var hackerEmail = '" +
            "document.write('<a href=\"mailto:'test@test.test'\">" +
            "</a>');" +
            "';";

    private HttpServletRequest request;
    private XSSRequestWrapper wrapper;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("input", new String[]{SCRIPT_ALERT_XSS_SCRIPT});
        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameterValues("input")).thenReturn(new String[]{COMPLEX_XSS_SCRIPT, HACKER_EMAIL});
        when(request.getParameter("input")).thenReturn(SCRIPT_ALERT_XSS_SCRIPT);

        wrapper = new XSSRequestWrapper(request);
    }

    @Test
    @DisplayName("Sanitize single parameter containing script tag")
    void testGetParameter() {
        assertEquals("&lt;script&gt;alert('xss')&lt;/script&gt;", wrapper.getParameter("input"));
    }

    @Test
    @DisplayName("Sanitize multiple parameter values with XSS content")
    void testGetParameterValues() {
        String[] values = wrapper.getParameterValues("input");
        assertEquals(2, values.length);
        assertEquals("&lt;img src=x onerror=alert('xss')&gt;", values[0]);
    }

    @Test
    @DisplayName("Sanitize hacker email script in parameter values")
    void testGetParameterHacker() {
        String[] values = wrapper.getParameterValues("input");
        assertEquals(2, values.length);
        assertFalse(values[1].contains("<script>"));
    }

    @Test
    @DisplayName("Sanitize parameter map with XSS content")
    void testGetParameterMap() {
        Map<String, String[]> parameterMap = wrapper.getParameterMap();
        assertEquals(1, parameterMap.size());
        assertEquals("&lt;script&gt;alert('xss')&lt;/script&gt;", parameterMap.get("input")[0]);
    }

    @Test
    @DisplayName("Return safe input unchanged")
    void testGetParameterWithNoXSS() {
        when(request.getParameter("input")).thenReturn("safeInput");
        assertEquals("safeInput", wrapper.getParameter("input"));
    }

    @Test
    @DisplayName("Return safe parameter values unchanged")
    void testGetParameterValuesWithNoXSS() {
        when(request.getParameterValues("input")).thenReturn(new String[]{"safeInput"});
        String[] values = wrapper.getParameterValues("input");
        assertEquals(1, values.length);
        assertEquals("safeInput", values[0]);
    }

    @Test
    @DisplayName("Return safe parameter map unchanged")
    void testGetParameterMapWithNoXSS() {
        Map<String, String[]> safeParameterMap = new HashMap<>();
        safeParameterMap.put("input", new String[]{"safe@Input.com"});
        when(request.getParameterMap()).thenReturn(safeParameterMap);

        Map<String, String[]> parameterMap = wrapper.getParameterMap();
        assertEquals(1, parameterMap.size());
        assertEquals("safe@Input.com", parameterMap.get("input")[0]);
    }

    @Test
    @DisplayName("Sanitize multiple parameters in map")
    void testMultipleParameters() {
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("param1", new String[]{SCRIPT_ALERT_XSS_SCRIPT});
        parameterMap.put("param2", new String[]{COMPLEX_XSS_SCRIPT});
        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameterValues("param1")).thenReturn(new String[]{SCRIPT_ALERT_XSS_SCRIPT});
        when(request.getParameterValues("param2")).thenReturn(new String[]{COMPLEX_XSS_SCRIPT});

        wrapper = new XSSRequestWrapper(request);

        String[] param1Values = wrapper.getParameterValues("param1");
        String[] param2Values = wrapper.getParameterValues("param2");

        assertEquals("&lt;script&gt;alert('xss')&lt;/script&gt;", param1Values[0]);
        assertEquals("&lt;img src=x onerror=alert('xss')&gt;", param2Values[0]);
    }

    @Test
    @DisplayName("Sanitize nested XSS in parameter value")
    void testNestedXSS() {
        String nestedXSS = "<img src=\"x\" onerror=\"alert('<script>alert(1)</script>')\">";
        when(request.getParameter("input")).thenReturn(nestedXSS);
        wrapper = new XSSRequestWrapper(request);

        String sanitizedValue = wrapper.getParameter("input");
        assertFalse(sanitizedValue.contains("<script>"));
    }

    @Test
    @DisplayName("Handle empty and null parameter values gracefully")
    void testEmptyAndNullValues() {
        when(request.getParameter("input")).thenReturn(null);
        wrapper = new XSSRequestWrapper(request);

        String sanitizedValue = wrapper.getParameter("input");
        assertNull(sanitizedValue);

        when(request.getParameter("input")).thenReturn("");
        sanitizedValue = wrapper.getParameter("input");
        assertEquals("", sanitizedValue);
    }
}