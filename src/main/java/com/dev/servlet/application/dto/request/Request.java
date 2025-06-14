package com.dev.servlet.application.dto.request;

import com.dev.servlet.application.dto.records.KeyPair;
import com.dev.servlet.application.dto.records.Query;

import java.util.List;
import java.util.Optional;

/**
 * This record represents an internal request.
 *
 * @since 1.4
 */
public record Request(
        String endpoint,
        String method,
        List<KeyPair> body,
        String token,
        Query query,
        int retry
) {
    public static Request of(String endpoint, String method, List<KeyPair> body, String token, Query query, int retry) {
        return new Request(endpoint, method, body, token, query, retry);
    }

    public Request withToken() {
        return new Request(null, null, null, token, null, 0);
    }

    public String getParameter(String name) {
        return getParam(name)
                .map(value -> ((String) value).trim())
                .orElse(null);
    }

    private Optional<Object> getParam(String name) {
        if (body == null) {
            return Optional.empty();
        }

        return body.stream()
                .filter(p -> p.key().equalsIgnoreCase(name))
                .map(KeyPair::getValue)
                .findFirst();
    }

    public String id() {
        return getParameter("id");
    }
}