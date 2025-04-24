package com.dev.servlet.controller.base;

import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.annotation.Property;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.core.util.PropertiesUtil;
import com.dev.servlet.core.validator.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class BaseRouterControllerTest {

    private TestController controller;
    private Request request;
    private EndpointParser endpoint;

    @BeforeEach
    void setUp() {
        controller = new TestController();
        request = mock(Request.class);
        endpoint = mock(EndpointParser.class);

        when(endpoint.getEndpoint()).thenReturn("test");
        when(endpoint.getController()).thenReturn("TestController");
        when(endpoint.getApiVersion()).thenReturn("v1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getToken()).thenReturn("valid-token");
    }

    @Test
    void route_WithValidEndpoint_ShouldCallCorrectMethod() throws Exception {
        // Arrange
        try (MockedStatic<CryptoUtils> cryptoUtils = mockStatic(CryptoUtils.class)) {
            cryptoUtils.when(() -> CryptoUtils.isValidToken("valid-token")).thenReturn(true);

            // Act
            IHttpResponse<?> response = controller.route(endpoint, request);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.statusCode());
            assertEquals("Test response", response.body());
        }
    }

    @Test
    void route_WithValidEndpointAndPropertyParam_ShouldPassPropertyValue() throws Exception {
        // Arrange
        when(endpoint.getEndpoint()).thenReturn("testWithProperty");

        try (MockedStatic<CryptoUtils> cryptoUtils = mockStatic(CryptoUtils.class);
             MockedStatic<PropertiesUtil> propertiesUtil = mockStatic(PropertiesUtil.class)) {

            cryptoUtils.when(() -> CryptoUtils.isValidToken("valid-token"))
                    .thenReturn(true);

            propertiesUtil.when(() -> PropertiesUtil.getProperty(eq("test.property"), anyString()))
                    .thenReturn("property-value");

            propertiesUtil.when(() -> PropertiesUtil.getProperty(eq("env"), anyString()))
                    .thenReturn("development");

            // Act
            IHttpResponse<?> response = controller.route(endpoint, request);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.statusCode());
            assertEquals("Property: property-value", response.body());
        }
    }

    @Test
    void route_WithInvalidEndpoint_ShouldThrowException() {
        // Arrange
        when(endpoint.getEndpoint()).thenReturn("nonexistent");

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> controller.route(endpoint, request));
        assertEquals(501, exception.getCode());
        assertEquals("Endpoint not implemented: nonexistent", exception.getMessage());
    }

    @Test
    void route_WithValidEndpointButValidationFails_ShouldThrowException() {
        // Arrange
        try (MockedStatic<RequestValidator> validator = mockStatic(RequestValidator.class)) {
            validator.when(() -> RequestValidator.validate(any(), any(), any()))
                    .thenThrow(new ServiceException(400, "Validation failed"));

            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> controller.route(endpoint, request));
            assertEquals(400, exception.getCode());
            assertEquals("Validation failed", exception.getMessage());

            // Verify that validate was called
            validator.verify(() -> RequestValidator.validate(eq(endpoint), any(), eq(request)), times(1));
        }
    }

    /**
     * Test controller implementation for testing BaseRouterController
     */
    private static class TestController extends BaseRouterController {

        @RequestMapping(value = "/test")
        public IHttpResponse<String> test(Request request) {
            return HttpResponse.ok("Test response").build();
        }

        @RequestMapping(value = "/testWithProperty")
        public IHttpResponse<String> testWithProperty(Request request, @Property("test.property") String property) {
            return HttpResponse.ok("Property: " + property).build();
        }
    }
}
