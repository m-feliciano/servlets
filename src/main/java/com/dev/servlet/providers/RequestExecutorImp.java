package com.dev.servlet.providers;

import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.interfaces.IRequestExecutor;
import com.dev.servlet.interfaces.ResourceMapping;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import com.dev.servlet.utils.BeanUtil;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * It's representing the business logic layer entry point.
 *
 * @author marcelo.feliciano
 * @since 1.0.0
 */
@Singleton
@Named("RequestExecutor")
public class RequestExecutorImp implements IRequestExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestExecutorImp.class);

    /**
     * Calls the appropriate service method based on the request.
     *
     * @param request {@link Request}
     * @param token   {@link String}
     * @return {@link Response}
     * @author marcelo.feliciano
     */
    public Response call(Request request, String token) {
        Response response;

        try {
            String[] parts = request.getEndpoint().split("/");

            Object instance = getServiceInstance(parts[0]);
            if (instance == null) {
                throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "Check your URL.");
            }

            Method method = getServiceMethod(parts[1], instance);
            if (method == null) {
                throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "Check your URL.");
            }

            Object[] args = {request, token}; // Can be extended to include more arguments as needed
            response = (Response) invokeActionMethod(instance, method, args);

        } catch (Exception e) {
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            String message = "Internal server error. Contact support";

            if (e instanceof ServiceException || e.getCause() instanceof ServiceException) {
                ServiceException c = (ServiceException) (e instanceof ServiceException ? e : e.getCause());
                status = c.getCode();
                message = c.getMessage();
            } else {
                LOGGER.error(e.getMessage(), e);
            }

            response = Response.ofError(status, message);
        }

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
        List<Method> methods = MethodUtils.getMethodsListWithAnnotation(object.getClass(), ResourceMapping.class);
        return methods.stream()
                .filter(m -> m.getAnnotation(ResourceMapping.class).value().equals(serviceName))
                .findFirst()
                .orElse(null);
    }
}