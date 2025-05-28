package com.dev.servlet.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EndpointParser {

    private static final int API_VERSION_INDEX = 2;
    private static final int SERVICE_NAME_START_INDEX = 4;

    private String service;
    private String apiVersion;
    private String serviceName;

    private EndpointParser(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("Request or endpoint cannot be null or empty");
        }

        String[] parts = endpoint.split("/");
        if (parts.length < SERVICE_NAME_START_INDEX) {
            throw new IllegalArgumentException("Invalid endpoint format: " + endpoint);
        }

        init(parts);
    }

    public static EndpointParser of(String path) {
        return new EndpointParser(path);
    }

    private void init(String[] parts) {
        // apiVersion is at index 2
        this.apiVersion = parts[API_VERSION_INDEX];

        // service is at index 3 (with leading slash)
        this.service = "/" + parts[3];

        // serviceName is everything after SERVICE_NAME_START_INDEX joined by "/"
        this.serviceName = String.join("/",
                Arrays.copyOfRange(parts, SERVICE_NAME_START_INDEX, parts.length));
    }
}
