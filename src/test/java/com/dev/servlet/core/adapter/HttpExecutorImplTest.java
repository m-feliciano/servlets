package com.dev.servlet.core.adapter;

import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.application.transfer.response.IHttpResponse;
import com.dev.servlet.core.interfaces.IHttpExecutor;
import com.dev.servlet.core.util.BeanUtil;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.presentation.controller.base.BaseRouterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HttpExecutorImplTest {

    private IHttpExecutor<?> httpExecutor;
    private Request request;
    private EndpointParser parser;

    @BeforeEach
    void setUp() {
        httpExecutor = HttpExecutorImpl.newInstance();
        request = mock(Request.class);
        parser = mock(EndpointParser.class);

        when(parser.getApiVersion()).thenReturn("v1");
        when(parser.getController()).thenReturn("/testService");
        when(parser.getEndpoint()).thenReturn("/test");
        when(request.endpoint()).thenReturn("/api/v1/testService/test");
    }

    @Test
    @DisplayName(
            "Test send method with a valid request and a successful response. " +
            "It should return the expected IHttpResponse object.")
    void testSend_Success() throws Exception {
        BaseRouterController controller = mock(BaseRouterController.class);
        IHttpResponse<Object> expectedResponse = HttpResponse.ok().next("Next").build();

        when(request.endpoint()).thenReturn("/test");

        try (MockedStatic<EndpointParser> parserMock = mockStatic(EndpointParser.class);
             MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {

            // Mock the static method to return the mocked parser
            parserMock.when(() -> EndpointParser.of(anyString())).thenReturn(parser);

            when(BeanUtil.getResolver()).thenReturn(mock(BeanUtil.DependencyResolver.class));
            beanUtilMock.when(() -> BeanUtil.getResolver().getService("/testService")).thenReturn(controller);

            when(controller.route(parser, request)).thenReturn(expectedResponse);

            IHttpResponse<?> response = httpExecutor.send(request);

            assertNotNull(response);
            assertEquals(expectedResponse, response);
            assertEquals("Next", response.next());
            assertNull(response.errors());
        }
    }

    @Test
    @DisplayName(
            "Test send method with a valid request and an error response. " +
            "It should return the expected IHttpResponse object with errors.")
    void testSend_ServiceException() {
        try (MockedStatic<EndpointParser> parserMock = mockStatic(EndpointParser.class);
             MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {

            parserMock.when(() -> EndpointParser.of(anyString())).thenReturn(parser);
            beanUtilMock.when(() -> BeanUtil.getResolver().getService("testService")).thenReturn(null);

            IHttpResponse<?> response = httpExecutor.send(request);

            assertNotNull(response);
            assertEquals(400, response.statusCode());
            assertEquals("Error resolving service method for path: /testService", response.errors().iterator().next());
        }
    }

    @Test
    @DisplayName(
            "Test send method with a valid request and an unexpected exception. " +
            "It should return an IHttpResponse object with a 500 status code.")
    void testSend_UnexpectedException() {
        when(request.endpoint()).thenReturn("/test");
        try (MockedStatic<EndpointParser> parserMock = mockStatic(EndpointParser.class)) {

            parserMock.when(() -> EndpointParser.of("/test")).thenThrow(new RuntimeException("Unexpected error"));

            IHttpResponse<?> response = httpExecutor.send(request);

            assertNotNull(response);
            assertEquals(500, response.statusCode());
            assertEquals("An unexpected error occurred.", response.errors().iterator().next());
        }
    }
}

