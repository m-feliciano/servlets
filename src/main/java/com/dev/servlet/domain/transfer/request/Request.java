package com.dev.servlet.domain.transfer.request;

import com.dev.servlet.domain.transfer.records.KeyPair;
import com.dev.servlet.domain.transfer.records.Query;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class Request {
    private final String endpoint;
    private final String method;
    private final List<KeyPair> body;
    private final String token;
    private final Query query;
    private final int retry;
    public Request withToken() {
        return Request.builder().token(token).build();
    }

    public String getParameter(String name) {
        Optional<String> optional = getParam(name).map(value -> ((String) value).trim());
        return optional.orElse(null);
    }

    public String id() {
        return getParameter("id");
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
}
