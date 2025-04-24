package com.dev.servlet.controller.base;

import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.annotation.Property;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.core.util.PropertiesUtil;
import com.dev.servlet.core.validator.RequestValidator;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static com.dev.servlet.core.util.ThrowableUtils.throwServiceError;

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

    public <U> IHttpResponse<U> route(EndpointParser endpoint, Request request) throws Exception {
        var routeMapping = routeMappingFromEndpoint(endpoint);
        var requestMapping = routeMapping.method().getAnnotation(RequestMapping.class);
        RequestValidator.validate(endpoint, requestMapping, request);
        Object[] args = prepareMethodArguments(routeMapping, request);
        return invokeServiceMethod(this, routeMapping.method(), args);
    }

    private RouteMapping routeMappingFromEndpoint(EndpointParser endpoint) throws ServiceException {
        var routeMapping = routeMappings.get(endpoint.getEndpoint());
        if (routeMapping == null) {
            throwServiceError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Endpoint not implemented: " + endpoint.getEndpoint());
        }
        return routeMapping;
    }

    private Object[] prepareMethodArguments(RouteMapping mapping, Request request) throws ServiceException {
        Method method = mapping.method();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[mapping.parameterTypes().length];
        for (int i = 0; i < mapping.parameterTypes().length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Property.class)) {
                String propertyKey = parameter.getAnnotation(Property.class).value();
                args[i] = PropertiesUtil.getProperty(propertyKey, "");
            } else {
                Class<?> parameterType = mapping.parameterTypes()[i];
                args[i] = resolveArgument(parameterType, request);
            }
        }
        return args;
    }

    private Object resolveArgument(Class<?> parameter, Request request) throws ServiceException {
        if (Request.class.isAssignableFrom(parameter)) {
            return request;
        }
        if (request.getBody() != null) {
            return request.getBody()
                    .stream()
                    .filter(body -> parameter.isAssignableFrom(body.getClass()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private <U> IHttpResponse<U> invokeServiceMethod(Object instance, Method method, Object[] args) throws Exception {
        @SuppressWarnings("ALL")
        var response = (IHttpResponse<U>) method.invoke(instance, args);
        return response;
    }
}
