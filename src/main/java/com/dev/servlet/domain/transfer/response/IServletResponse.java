package com.dev.servlet.domain.transfer.response;

import com.dev.servlet.domain.transfer.records.KeyPair;

import java.util.Optional;
import java.util.Set;

/**
 * Servlet-specific HTTP response interface that extends the generic HTTP response.
 * Provides specialized functionality for handling servlet responses with key-value pairs.
 * 
 * <p>This interface extends the generic HTTP response interface to provide servlet-specific
 * response handling capabilities. It uses a Set of KeyPair objects as the response body,
 * enabling structured data transfer with convenient entity retrieval methods.
 * The interface provides default implementations for common servlet response patterns.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface IServletResponse extends IHttpResponse<Set<KeyPair>> {
    
    /**
     * Retrieves an entity value by its key from the response body.
     * This method searches through the KeyPair set to find a matching key
     * and returns the associated value.
     *
     * @param key the key to search for in the response body
     * @return the value associated with the key, or null if key not found
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
    
    /**
     * Default implementation returns null for error information.
     * Servlet responses typically handle errors through status codes.
     *
     * @return null as servlet responses don't use separate error messages
     */
    @Override
    default String error() {
        return null;
    }
    
    /**
     * Default implementation returns null for reason text.
     * Servlet responses rely on standard HTTP status code meanings.
     *
     * @return null as servlet responses use standard HTTP reason phrases
     */
    @Override
    default String reasonText() {
        return null;
    }
}
