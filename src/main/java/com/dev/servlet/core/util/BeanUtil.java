package com.dev.servlet.core.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BeanUtil {

    public static final String CONTROLLER_PACKAGE_NAME = "com.dev.servlet.presentation.controller.";
    private static final Map<String, Class<?>> services = new ConcurrentHashMap<>();

    public static <T> T getBean(Class<T> beanType) {
        return getResolver().resolve(beanType);
    }

    public static <T> T getBean(Class<T> beanType, Annotation... qualifiers) {
        return getResolver().resolve(beanType, qualifiers);
    }

    public static DependencyResolver getResolver() {
        return ResolverHolder.resolver;
    }

    private static final class ResolverHolder {
        private static final DependencyResolver resolver = new DependencyResolver();
        private static final BeanManager beanManager = CDI.current().getBeanManager();
    }

    public static class DependencyResolver {

        public <T> T resolve(Class<T> beanType) {
            return resolve(beanType, new Annotation[0]);
        }

        @SuppressWarnings("unchecked")
        public <T> T resolve(Class<T> beanType, Annotation... qualifiers) {
            try {
                BeanManager bm = ResolverHolder.beanManager;
                Set<Bean<?>> beans = bm.getBeans(beanType, qualifiers);
                if (beans.isEmpty()) {
                    log.warn("No beans found for type: {}", beanType.getName());
                    return null;
                }

                Bean<?> bean = bm.resolve(beans);
                CreationalContext<?> ctx = bm.createCreationalContext(bean);
                return (T) bm.getReference(bean, beanType, ctx);
            } catch (Exception e) {
                log.error("Failed to resolve bean: {}", beanType.getName(), e);
                return null;
            }
        }

        public Object getService(String service) {
            if (services.containsKey(service)) {
                return getBean(services.get(service));
            }

            try {
                Class<?> serviceClass;
                try {
                    serviceClass = ClassUtils.getClass(CONTROLLER_PACKAGE_NAME + service);
                } catch (ClassNotFoundException e) {
                    log.error("Failed to load service class: {}", service, e);
                    return null;
                }

                services.put(service, serviceClass);
                return resolve(serviceClass);
            } catch (Exception e) {
                log.error("Error resolving service: {}", service, e);
                return null;
            }
        }
    }
}