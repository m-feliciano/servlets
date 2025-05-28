package com.dev.servlet.adapter;

import com.dev.servlet.model.pojo.records.KeyPair;

import java.util.Optional;
import java.util.Set;

/**
 * Servlet Response
 * <p>
 * This interface is responsible for a list of response data.
 *
 * @see IHttpResponse
 */
public interface IServletResponse extends IHttpResponse<Set<KeyPair>> {

    /**
     * Retrieve a response by key
     *
     * @param key the key to search for
     * @return the value of the key
     */
    default Object getEntity(String key) {
        return Optional.of(body())
                .flatMap(response -> response.stream()
                        .filter(pair -> pair.getKey().equals(key))
                        .findFirst()
                        .map(KeyPair::getValue)
                )
                .orElse(null);
    }

    default Set<String> errors() {
        return null;
    }
}