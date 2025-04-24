package com.dev.servlet.adapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Functional interface for dispatching HTTP servlet requests to appropriate handlers.
 * 
 * <p>This interface defines the contract for servlet request dispatching in the adapter layer.
 * It follows the functional programming paradigm and provides a way to abstract servlet
 * request handling logic. The interface serves as a bridge between the servlet container
 * and the application's request processing logic, enabling flexible dispatch strategies.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * IServletDispatcher dispatcher = (req, resp) -> {
 *     // Process request and generate response
 *     RequestProcessor.process(req, resp);
 * };
 * dispatcher.dispatch(request, response);
 * }</pre>
 * 
 * @author servlets-team
 * @since 1.0
 */
@FunctionalInterface
public interface IServletDispatcher {
    
    /**
     * Dispatches an HTTP servlet request to the appropriate handler.
     * This method processes the incoming request and generates an appropriate response.
     *
     * @param httpServletRequest the HTTP servlet request to be processed
     * @param httpServletResponse the HTTP servlet response to be populated
     */
    void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
