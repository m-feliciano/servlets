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

/**
 * Utility class for CDI (Contexts and Dependency Injection) bean resolution and management.
 * This class provides a simplified interface for programmatic bean lookup and dependency
 * resolution, abstracting the complexity of CDI BeanManager operations.
 * 
 * <p>The utility supports:
 * <ul>
 *   <li>Type-safe bean resolution with optional qualifiers</li>
 *   <li>Dynamic service loading by name with caching</li>
 *   <li>Automatic controller package resolution</li>
 *   <li>Thread-safe bean caching for performance</li>
 * </ul>
 * 
 * <p>Bean resolution follows CDI standard practices:
 * <ul>
 *   <li>Beans are resolved by type and optional qualifiers</li>
 *   <li>Ambiguous resolution is handled by CDI resolver</li>
 *   <li>CreationalContext manages bean lifecycle</li>
 *   <li>Dependency cycles are handled by CDI container</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Type-safe bean resolution
 * IUserService userService = BeanUtil.getBean(IUserService.class);
 * 
 * // Bean resolution with qualifiers
 * IUserService qualifiedService = BeanUtil.getBean(IUserService.class, someQualifier);
 * 
 * // Dynamic service resolution by name
 * DependencyResolver resolver = BeanUtil.getResolver();
 * Object service = resolver.getService("UserController");
 * 
 * // Direct controller resolution
 * UserController controller = (UserController) resolver.getService("UserController");
 * }
 * </pre>
 * 
 * <p><strong>Performance Note:</strong> Service classes resolved by name are cached
 * to avoid repeated ClassLoader operations. The cache is thread-safe and persistent
 * for the application lifecycle.
 * 
 * @since 1.0
 * @see javax.enterprise.inject.spi.CDI
 * @see javax.enterprise.inject.spi.BeanManager
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BeanUtil {

    private static final String CONTROLLER_PACKAGE_NAME = "com.dev.servlet.controller.";
    private static final Map<String, Class<?>> services = new ConcurrentHashMap<>();

    /**
     * Resolves a CDI bean by type without qualifiers.
     * This is a convenience method for the most common bean resolution scenario.
     * 
     * @param <T> the bean type
     * @param beanType the class of the bean to resolve
     * @return the resolved bean instance, or null if not found
     */
    private static <T> T getBean(Class<T> beanType) {
        return getResolver().resolve(beanType);
    }

    /**
     * Resolves a CDI bean by type with optional qualifiers.
     * This method supports complex dependency resolution scenarios where
     * multiple implementations exist for the same interface.
     * 
     * @param <T> the bean type
     * @param beanType the class of the bean to resolve
     * @param qualifiers optional qualifier annotations to narrow the resolution
     * @return the resolved bean instance, or null if not found
     */
    public static <T> T getBean(Class<T> beanType, Annotation... qualifiers) {
        return getResolver().resolve(beanType, qualifiers);
    }

    /**
     * Gets the singleton DependencyResolver instance for advanced bean operations.
     * The resolver provides additional capabilities beyond basic bean resolution.
     * 
     * @return the dependency resolver instance
     */
    public static DependencyResolver getResolver() {
        return ResolverHolder.resolver;
    }

    /**
     * Holder class for singleton pattern implementation.
     * Uses lazy initialization and thread-safe singleton pattern.
     */
    private static final class ResolverHolder {
        private static final DependencyResolver resolver = new DependencyResolver();
        private static final BeanManager beanManager = CDI.current().getBeanManager();
    }

    /**
     * Dependency resolver that encapsulates CDI BeanManager operations.
     * This class provides both type-based and name-based bean resolution
     * with comprehensive error handling and caching.
     */
    public static class DependencyResolver {
        
        /**
         * Resolves a bean by type without qualifiers.
         * 
         * @param <T> the bean type
         * @param beanType the class of the bean to resolve
         * @return the resolved bean instance, or null if resolution fails
         */
        public <T> T resolve(Class<T> beanType) {
            return resolve(beanType, new Annotation[0]);
        }
        
        /**
         * Resolves a bean by type with optional qualifiers using CDI BeanManager.
         * This method handles the complete CDI resolution lifecycle including
         * bean lookup, ambiguity resolution, and instance creation.
         * 
         * @param <T> the bean type
         * @param beanType the class of the bean to resolve
         * @param qualifiers optional qualifier annotations
         * @return the resolved bean instance, or null if resolution fails
         */
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

        /**
         * Resolves a service by name, primarily used for controller resolution.
         * This method implements a caching strategy to avoid repeated class loading
         * operations and improve performance for frequently accessed services.
         * 
         * <p>The resolution process:
         * <ol>
         *   <li>Check cache for previously loaded service class</li>
         *   <li>If not cached, attempt to load class from controller package</li>
         *   <li>Cache the loaded class for future use</li>
         *   <li>Resolve bean instance using CDI</li>
         * </ol>
         * 
         * @param service the service name (typically matches class name)
         * @return the resolved service instance, or null if not found
         */
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
