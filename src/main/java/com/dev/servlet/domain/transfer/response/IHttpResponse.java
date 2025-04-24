package com.dev.servlet.domain.transfer.response;

/**
 * Generic HTTP response interface providing standard HTTP response structure.
 * 
 * <p>This interface defines the contract for HTTP responses in the application,
 * encapsulating status codes, response bodies, error information, and navigation data.
 * It provides a standardized way to handle HTTP responses across different layers
 * of the application, ensuring consistent response handling patterns.</p>
 * 
 * @param <TResponse> the type of the response body content
 * @author servlets-team
 * @since 1.0
 */
public interface IHttpResponse<TResponse> {
    
    /**
     * Returns the HTTP status code of the response.
     *
     * @return the HTTP status code (e.g., 200, 404, 500)
     */
    int statusCode();
    
    /**
     * Returns the response body content.
     *
     * @return the response body of type TResponse
     */
    TResponse body();
    
    /**
     * Returns error information if the response represents an error state.
     *
     * @return error message or description, null if no error occurred
     */
    String error();
    
    /**
     * Returns the reason text associated with the HTTP status code.
     *
     * @return human-readable description of the status code
     */
    String reasonText();
    
    /**
     * Returns navigation information for paginated or linked responses.
     *
     * @return URL or identifier for the next page/resource, null if not applicable
     */
    String next();
}
