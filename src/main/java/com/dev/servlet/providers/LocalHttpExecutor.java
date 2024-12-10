package com.dev.servlet.providers;

import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.interfaces.IHttpExecutor;
import com.dev.servlet.interfaces.IHttpResponse;
import com.dev.servlet.interfaces.RequestMapping;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.BeanUtil;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for executing the HTTP request.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
public class LocalHttpExecutor implements IHttpExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalHttpExecutor.class);
    private static final String ERROR_CHECK_YOUR_URL = "Check your URL.";
    private final Request request;

    public LocalHttpExecutor(Request request) {
        this.request = request;
    }

    /**
     * Calls the appropriate service method based on the request.
     *
     * @return {@link HttpResponse}
     * @author marcelo.feliciano
     */
    public IHttpResponse<Object> call() {
        IHttpResponse<Object> response;

        try {
            // /category/new -> [category, new]
            String[] parts = parserEndpoint();

            Object instance = this.getServiceInstance("/".concat(parts[0]));
            if (instance == null) {
                throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, ERROR_CHECK_YOUR_URL);
            }

            String endpoint = parts.length > 1 ? "/".concat(parts[1]) : "/";

            Method method = getServiceMethod(endpoint, instance);
            if (method == null) {
                throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, ERROR_CHECK_YOUR_URL);
            }

            this.validateRequestMethod(method);

            Object[] args = {request}; // Can be extended to include more arguments as needed
            response = invokeMethod(instance, method, args);

        } catch (ServiceException e) {
            return handleServiceException(e);
        } catch (Exception e) {
            return handleGenericException(e);
        }

        return response;
    }

    /**
     * Handles the service exception.
     *
     * @param serviceException
     * @return {@link IHttpResponse} with the error message
     */
    private IHttpResponse<Object> handleServiceException(ServiceException serviceException) {
        return HttpResponse.ofError(serviceException.getCode(), serviceException.getMessage());
    }

    /**
     * Handles the generic exception.
     *
     * @param exception
     * @return {@link IHttpResponse} with the error message
     */
    private IHttpResponse<Object> handleGenericException(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        return HttpResponse.ofError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error. Contact support");
    }

    /**
     * Parses the endpoint to get the service and action.
     *
     * @return {@link String[]}
     */
    private String[] parserEndpoint() {
        return Arrays.stream(request.getEndpoint().split("/"))
                .skip(1)
                .toArray(String[]::new);
    }

    /**
     * Validates the request method.
     *
     * @param method {@link Method}
     */
    private void validateRequestMethod(Method method) throws ServiceException {
        RequestMapping mapping = method.getDeclaredAnnotation(RequestMapping.class);

        if (!mapping.method().equals(request.getMethod())) {
            throw new ServiceException(400, "Method not allowed. Expected: " + mapping.method() + " but got: " + request.getMethod());
        }
    }

    /**
     * Invokes the method in the given instance.
     *
     * @param instance {@link Object}
     * @param method   {@link Method}
     * @param args     {@link Object[]} the arguments to pass to the method
     * @return the result of the method invocation
     * @throws Exception if an error occurs during the method invocation
     */
    @SuppressWarnings("unchecked")
    private static IHttpResponse<Object> invokeMethod(Object instance, Method method, Object[] args) throws Exception {
        IHttpResponse<Object> response;
        response = (IHttpResponse<Object>) invokeActionMethod(instance, method, args);
        return response;
    }

    private Object getServiceInstance(String service) {
        // Get the service instance from the dependency resolver
        var resolver = BeanUtil.getResolver();
        return resolver.getService(service);
    }

    /**
     * Finds the method corresponding to the action in the given object.
     *
     * @param instance {@link Object}
     * @param method   {@link Method}
     * @param params   {@link Object[]} the arguments to pass to the method
     * @return the result of the method invocation
     * @author marcelo.feliciano
     */
    private static Object invokeActionMethod(Object instance, Method method, Object[] params) throws Exception {
        Object[] args = new Object[method.getParameters().length];

        for (int i = 0; i < args.length; i++) {
            Parameter parameter = method.getParameters()[i];
            args[i] = Arrays.stream(params)
                    .filter(d -> d != null && d.getClass().equals(parameter.getType()))
                    .map(parameter.getType()::cast)
                    .findFirst()
                    .orElse(null);
        }

        return method.invoke(instance, args);
    }

    /**
     * Finds the method corresponding to the action in the given object.
     *
     * @param serviceName as defined in the ResourceMapping annotation
     * @param object      an instance of the object
     * @return the method corresponding or null
     * @author marcelo.feliciano
     */
    private static Method getServiceMethod(String serviceName, Object object) {
        List<Method> methods = MethodUtils.getMethodsListWithAnnotation(object.getClass(), RequestMapping.class);

        return methods.stream()
                .filter(m -> m.getAnnotation(RequestMapping.class).value().equals(serviceName))
                .findFirst()
                .orElse(null);
    }
}