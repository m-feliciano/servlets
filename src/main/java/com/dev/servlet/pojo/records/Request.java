package com.dev.servlet.pojo.records;

import com.dev.servlet.dto.ServiceException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * This class represents an internal request
 *
 * @since 1.4
 */
@Getter
@Setter
@Builder()
public class Request {

    private final String endpoint;
    private final String method;
    private final List<KeyPair> body;
    private final String token;
    private final String entityId;
    private final Query query;
    private final int retry;

    public String getRequiredParameter(String name) throws ServiceException {
        Optional<Object> optional = getParam(name);

        return (String) optional
                .orElseThrow(() -> new ServiceException(name + " is required"));
    }

    public String getParameter(String name) {
        Optional<Object> optional = getParam(name);
        return optional.map(o -> ((String) o).trim()).orElse(null);
    }

    private Optional<Object> getParam(String name) {
        return body.stream()
                .filter(p -> p.key().equalsIgnoreCase(name))
                .map(KeyPair::getValue)
                .findFirst();
    }
}