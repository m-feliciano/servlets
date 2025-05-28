package com.dev.servlet.util;

import com.dev.servlet.controller.Controller;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for managing bean dependencies and service resolution.
 */
@SuppressWarnings({"unchecked"})
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BeanUtil {

    /**
     * This class is responsible for holding the singleton instance of the DependencyResolver and BeanManager.
     */
    private static final class ResolverHolder {
        private static final DependencyResolver resolver = new DependencyResolver();
        private static final BeanManager beanManager = CDI.current().getBeanManager();
    }

    /**
     * Retrieves the singleton instance of the DependencyResolver.
     *
     * @return {@linkplain DependencyResolver}
     */
    public static DependencyResolver getResolver() {
        return ResolverHolder.resolver;
    }

    /**
     * Service locator for resolving and managing dependencies.
     */
    public static class DependencyResolver {

        public static final String BUSINESS_PACKAGE = "com.dev.servlet.controller";
        private final Map<String, Class<?>> services = new ConcurrentHashMap<>();

        private DependencyResolver() {
            // Private constructor to prevent instantiation
        }

        /**
         * Retrieves an instance of the specified service.
         *
         * @param service the key of the service
         * @return the service instance
         */
        public Object getService(String service) {
            if (!services.containsKey(service)) {
                log.error("Service not found: {}", service);
                return null;
            }

            return resolve(services.get(service));
        }

        /**
         * Resolves all services annotated with {@linkplain Controller}.
         */
        @SuppressWarnings("unchecked")
        public synchronized void resolveAll() {
            try {
                List<Class<?>> clazzList = ClassUtil.scanPackage(BUSINESS_PACKAGE, new Class[]{Controller.class});
                for (Class<?> clazz : clazzList) {
                    String path = clazz.getAnnotation(Controller.class).path();
                    services.putIfAbsent(path, clazz);

                    resolve(clazz);
                    log.info("Resolved service: {}", clazz.getName());
                }

                log.info("All services have been resolved");
            } catch (Exception e) {
                log.error("Failed to load classes", e);
            }
        }

        /**
         * Resolves the specified bean type.
         *
         * @param beanType the class of the bean
         * @return the resolved bean instance
         */
        public Object resolve(Class<?> beanType) {
            try {
                BeanManager bm = ResolverHolder.beanManager;
                Bean<?> bean = bm.resolve(bm.getBeans(beanType));
                CreationalContext<?> ctx = bm.createCreationalContext(bean);
                return bm.getReference(bean, bean.getBeanClass(), ctx);
            } catch (Exception e) {
                log.error("Failed to resolve bean: {}", beanType.getName(), e);
                return null;
            }
        }

        /**
         * Resolves dependencies for the specified object instance.
         *
         * @param object {@linkplain Object}
         */
        public void resolve(Object object) {
            try {
                injectFields(object);
                injectMethods(object);
                invokePostConstructMethods(object);
            } catch (Exception e) {
                log.error("Failed to instantiate service: {}", object.getClass().getName(), e);
            }
        }

        private void injectMethods(Object object) throws Exception {
            List<Method> methods = MethodUtils.getMethodsListWithAnnotation(object.getClass(), Inject.class);
            for (Method method : methods) {
                Object[] parameters = resolveParameters(method.getParameterTypes());
                method.invoke(object, parameters);
            }
        }

        private void injectFields(Object object) throws Exception {
            Field[] fields = FieldUtils.getFieldsWithAnnotation(object.getClass(), Inject.class);
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object resolved = resolve(field.getType());
                    field.set(object, resolved);
                } finally {
                    field.setAccessible(false);
                }
            }
        }

        private void invokePostConstructMethods(Object object) throws Exception {
            List<Method> methods = MethodUtils.getMethodsListWithAnnotation(object.getClass(), PostConstruct.class);
            for (Method method : methods) {
                method.invoke(object);
            }
        }

        private Object[] resolveParameters(Class<?>[] parameterTypes) {
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = resolve(parameterTypes[i]);
            }
            return parameters;
        }
    }
}