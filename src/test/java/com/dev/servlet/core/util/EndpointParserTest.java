package com.dev.servlet.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EndpointParserTest {

    @Test
    @DisplayName(
            "Test EndpointParser with a valid endpoint. " +
            "It should parse the endpoint and return the correct API version, service, and service name.")
    void testValidEndpoint() {
        String endpoint = "/api/v1/serviceName/action";
        EndpointParser parser = EndpointParser.of(endpoint);

        assertEquals("v1", parser.getApiVersion());
        assertEquals("ServiceNameController", parser.getController());
        assertEquals("action", parser.getEndpoint());
    }

    @Test
    @DisplayName(
            "Test EndpointParser with a valid endpoint containing multiple service names. " +
            "It should parse and concatenate the service names correctly.")
    void testValidEndpointWithMultipleServiceNames() {
        String endpoint = "/api/v1/serviceName/action/subAction";
        EndpointParser parser = EndpointParser.of(endpoint);

        assertEquals("v1", parser.getApiVersion());
        assertEquals("ServiceNameController", parser.getController());
        assertEquals("action/subAction", parser.getEndpoint());
    }

    @Test
    @DisplayName(
            "Test EndpointParser with an invalid endpoint that is too short. " +
            "It should throw an IllegalArgumentException with the correct message.")
    void testInvalidEndpointTooShort() {
        String endpoint = "/api/v1";
        var exception = assertThrows(IllegalArgumentException.class, () -> EndpointParser.of(endpoint));

        assertEquals("Invalid endpoint format: /api/v1", exception.getMessage());
    }

    @Test
    @DisplayName(
            "Test EndpointParser with a null endpoint. " +
            "It should throw an IllegalArgumentException indicating the endpoint cannot be null or empty.")
    void testNullEndpoint() {
        var exception = assertThrows(IllegalArgumentException.class, () -> EndpointParser.of(null));
        assertEquals("Request or endpoint cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName(
            "Test EndpointParser with an empty endpoint. " +
            "It should throw an IllegalArgumentException indicating the endpoint cannot be null or empty.")
    void testEmptyEndpoint() {
        var exception = assertThrows(IllegalArgumentException.class, () -> EndpointParser.of(""));
        assertEquals("Request or endpoint cannot be null or empty", exception.getMessage());
    }
}

