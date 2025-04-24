package com.dev.servlet.adapter;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.IHttpResponse;

/**
 * Functional interface for executing HTTP operations and returning structured responses.
 * 
 * <p>This interface defines a contract for executing HTTP-related operations in the adapter layer.
 * It follows the functional programming paradigm and can be used with lambda expressions
 * or method references to create flexible HTTP execution strategies. The interface abstracts
 * the execution logic while providing a standardized way to handle requests and responses.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * IHttpExecutor<ProductDTO> executor = request -> productService.findById(request);
 * IHttpResponse<ProductDTO> response = executor.call(request);
 * }</pre>
 * 
 * @param <TResponse> the type of response data returned by the HTTP operation
 * @author servlets-team
 * @since 1.0
 */
@FunctionalInterface
public interface IHttpExecutor<TResponse> {
    
    /**
     * Executes an HTTP operation based on the provided request and returns a structured response.
     *
     * @param request the request containing operation parameters and data
     * @return IHttpResponse containing the operation result and metadata
     */
    IHttpResponse<TResponse> call(Request request);
}
