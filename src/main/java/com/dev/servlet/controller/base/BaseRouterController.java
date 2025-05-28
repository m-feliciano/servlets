package com.dev.servlet.controller.base;

import com.dev.servlet.adapter.IHttpResponse;
import com.dev.servlet.adapter.RequestMapping;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.util.BeanUtil;
import com.dev.servlet.util.EndpointParser;
import com.dev.servlet.validator.RequestValidator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.dev.servlet.util.ThrowableUtils.throwIfTrue;

public abstract class BaseRouterController {

    private final Map<String, RouteMapping> routeMappings = new HashMap<>();

    public record RouteMapping(Method method, Class<?>[] parameterTypes) {
        public RouteMapping(Method method) {
            this(method, method.getParameterTypes());
        }
    }

    protected BaseRouterController() {
        initRouteMapping();
    }

    private void initRouteMapping() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(RequestMapping.class)) {
                continue;
            }

            var requestMapping = method.getAnnotation(RequestMapping.class);
            String serviceController = requestMapping.value().substring(1);

            routeMappings.put(serviceController, new RouteMapping(method));
        }
    }

    /**
     * Routes the request to the appropriate service method based on the endpoint.
     *
     * @param endpoint The endpoint to route
     * @param request  The HTTP request
     * @throws Exception
     */
    public <U> IHttpResponse<U> route(EndpointParser endpoint, Request request) throws Exception {
        var routeMapping = routeMappingFromEndpoint(endpoint);
        var requestMapping = routeMapping.method().getAnnotation(RequestMapping.class);

        RequestValidator.validate(endpoint, requestMapping, request);

        Object[] args = prepareMethodArguments(routeMapping, request);
        return invokeServiceMethod(this, routeMapping.method(), args);
    }

    /**
     * Retrieves the service method based on the endpoint.
     *
     * @param endpoint The endpoint to route
     * @throws ServiceException
     */
    private RouteMapping routeMappingFromEndpoint(EndpointParser endpoint) throws ServiceException {
        var routeMapping = routeMappings.get(endpoint.getServiceName());
        throwIfTrue((routeMapping == null), 404, "Endpoint not found: " + endpoint.getServiceName());
        return routeMapping;
    }

    /**
     * Prepares the method arguments based on the request parameters.
     *
     * @param mapping  The service method
     * @param request The HTTP request
     * @return Array of method arguments
     */
    private Object[] prepareMethodArguments(RouteMapping mapping, Request request) throws ServiceException {
        Object[] args = new Object[mapping.parameterTypes().length];

        for (int i = 0; i < mapping.parameterTypes().length; i++) {
            Class<?> parameter = mapping.parameterTypes()[i];
            args[i] = resolveArgument(parameter, request);
        }

        return args;
    }

    /**
     * Resolves the argument based on the parameter type.
     *
     * @param parameter The method parameter
     * @param request   The HTTP request
     * @return The resolved argument
     */
    private Object resolveArgument(Class<?> parameter, Request request) throws ServiceException {
        if (Request.class.isAssignableFrom(parameter)) {
            return request;
        }

        if (BaseModel.class.isAssignableFrom(parameter)) {
            var baseModel = (BaseModel<?, ?>) BeanUtil.getResolver().resolve(parameter);
            throwIfTrue(baseModel == null, 500, "Error resolving the request parameter");
            return baseModel;
        }

        if (request.body() != null) {
            return request.body()
                    .stream()
                    .filter(body -> parameter.isAssignableFrom(body.getClass()))
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