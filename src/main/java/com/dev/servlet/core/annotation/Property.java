package com.dev.servlet.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Annotation for injecting configuration properties from external sources.
 * This annotation enables automatic property resolution and injection at runtime,
 * supporting fields, method parameters, and method return values.
 * 
 * <p>The property values are resolved from multiple sources in order of precedence:
 * <ol>
 *   <li>System properties (-Dproperty.name=value)</li>
 *   <li>Environment variables</li>
 *   <li>Configuration files (application.properties, etc.)</li>
 *   <li>Default values defined in code</li>
 * </ol>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * public class DatabaseService {
 *     
 *     public void connect(@Property("database.username") String username,
 *                        @Property("database.password") String password) {
 *         // Connect using injected properties
 *     }
 * }
 * }
 * </pre>
 * 
 * <p>Property names support dot notation for hierarchical configuration:
 * <ul>
 *   <li>{@code server.port} - Simple property</li>
 *   <li>{@code database.connection.pool.size} - Nested property</li>
 *   <li>{@code cache.timeout.minutes} - Specific configuration</li>
 * </ul>
 * 
 * @since 1.0
 * @see com.dev.servlet.core.util.PropertiesUtil
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, PARAMETER, METHOD})
public @interface Property {
    
    /**
     * The property key to resolve from configuration sources.
     * Supports dot notation for hierarchical properties (e.g., "database.url").
     * The key should match exactly the property name in configuration files
     * or system properties.
     * 
     * @return the property key to resolve
     */
    String value();
}
