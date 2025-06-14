package com.dev.servlet.core.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EndpointParser {

    private static final int API_VERSION_INDEX = 2;
    private static final int SERVICE_NAME_START_INDEX = 4;

    private String controller;
    private String apiVersion;
    private String endpoint;

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
        this.apiVersion = parts[API_VERSION_INDEX];
        this.controller = StringUtils.capitalize(parts[3]).concat("Controller");
        this.endpoint = String.join("/",
                Arrays.copyOfRange(parts, SERVICE_NAME_START_INDEX, parts.length));
    }
}