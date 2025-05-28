package com.dev.servlet.controller.base;

import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.RequestMapping;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.validator.RequestValidator;
import com.dev.servlet.util.EndpointParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRouterController {

    private final Map<String, Method> routeMappings = new HashMap<>();

    protected BaseRouterController() {
        // Mapeia os métodos anotados com @RequestMapping
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(RequestMapping.class)) {
                continue;
            }

            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            routeMappings.put(mapping.value().substring(1), method);
        }
    }

    /**
     * Routes the request to the appropriate service method based on the endpoint.
     *
     * @param endpoint The endpoint to route
     * @param request  The HTTP request
     * @return
     * @throws Exception
     */
    public <U> IHttpResponse<U> route(EndpointParser endpoint, Request request) throws Exception {
        Method method = getEndpointMethod(endpoint);

        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        RequestValidator.validate(endpoint, mapping, request);

        Object[] args = prepareMethodArguments(method, request);
        return invokeServiceMethod(this, method, args);
    }

    /**
     * Retrieves the service method based on the endpoint.
     *
     * @param endpoint The endpoint to route
     * @return
     * @throws ServiceException
     */
    private Method getEndpointMethod(EndpointParser endpoint) throws ServiceException {
        Method method = routeMappings.get(endpoint.getServiceName());
        if (method == null) {
            throw ServiceException.badRequest("Method not found for endpoint: " + endpoint.getServiceName());
        }

        return method;
    }

    /**
     * Prepares the method arguments based on the request parameters.
     *
     * @param method  The service method
     * @param request The HTTP request
     * @return Array of method arguments
     */
    private Object[] prepareMethodArguments(Method method, Request request) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> resolveArgument(parameter, request))
                .toArray();
    }

    /**
     * Resolves the argument based on the parameter type.
     *
     * @param parameter The method parameter
     * @param request   The HTTP request
     * @return The resolved argument
     */
    private Object resolveArgument(Parameter parameter, Request request) {
        if (parameter.getType().isAssignableFrom(Request.class)) {
            return request;
        }

        if (request.body() != null) {
            return request.body()
                    .stream()
                    .filter(body -> body.getClass().isAssignableFrom(parameter.getType()))
                    .findFirst()
                    .orElse(null);
        }

        // Add more argument resolution logic if needed
        return null;
    }

    /**
     * Invokes the service method with the provided arguments.
     *
     * @param instance The service instance
     * @param method   The service method
     * @param args     The method arguments
     * @return The HTTP response
     * @throws Exception if an error occurs during invocation
     */
    private <U> IHttpResponse<U> invokeServiceMethod(Object instance, Method method, Object[] args) throws Exception {
        @SuppressWarnings("ALL")
        var response = (IHttpResponse<U>) method.invoke(instance, args);
        return response;
    }
}