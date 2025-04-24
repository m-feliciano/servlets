package com.dev.servlet.core.util;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration properties utility providing type-safe property access and variable interpolation.
 * This class manages application configuration by loading properties files and providing
 * convenient methods for accessing typed property values with default fallbacks.
 * 
 * <p>Key features:
 * <ul>
 *   <li><strong>Variable interpolation:</strong> Supports property references like {@code ${other.property}}</li>
 *   <li><strong>Type-safe access:</strong> Automatic type conversion for common types</li>
 *   <li><strong>Default values:</strong> Fallback values when properties are missing</li>
 *   <li><strong>Collection support:</strong> Comma-separated values to collections</li>
 *   <li><strong>Environment profiles:</strong> Configurable property files via system property</li>
 * </ul>
 * 
 * <p>Property file resolution:
 * <ul>
 *   <li>Default: {@code app-prod.properties} from classpath root</li>
 *   <li>Override: Set system property {@code app.config.file} to specify different file</li>
 *   <li>Example: {@code -Dapp.config.file=app-dev.properties}</li>
 * </ul>
 * 
 * <p>Supported property types:
 * <ul>
 *   <li>String, Integer, Long, Double, Boolean</li>
 *   <li>Collections (comma-separated values)</li>
 *   <li>Variable interpolation with {@code ${property.name}} syntax</li>
 * </ul>
 * 
 * <p>Example properties file:</p>
 * <pre>
 * # Database configuration
 * database.host=localhost
 * database.port=5432
 * database.url=jdbc:postgresql://${database.host}:${database.port}/mydb
 * 
 * # Security settings
 * security.jwt.expiration=604800
 * security.enabled=true
 * 
 * # Allowed origins (collection)
 * cors.allowed.origins=http://localhost:3000,http://localhost:8080
 * </pre>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Simple property access
 * String dbUrl = PropertiesUtil.getProperty("database.url");
 * 
 * // Type-safe access with defaults
 * Integer timeout = PropertiesUtil.getProperty("connection.timeout", 30);
 * Boolean securityEnabled = PropertiesUtil.getProperty("security.enabled", false);
 * Long expiration = PropertiesUtil.getProperty("jwt.expiration", 3600L);
 * 
 * // Collection properties
 * List<String> origins = PropertiesUtil.getProperty("cors.allowed.origins", List.of());
 * 
 * // Variable interpolation (automatic)
 * String fullUrl = PropertiesUtil.getProperty("database.url"); 
 * // Returns: "jdbc:postgresql://localhost:5432/mydb"
 * }
 * </pre>
 * 
 * @since 1.0
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PropertiesUtil {

    private static final ConcurrentHashMap<String, String> propertiesCache = new ConcurrentHashMap<>();
    
    /**
     * Retrieves a property value as a string with variable interpolation support.
     * This method handles nested property references using ${property.name} syntax
     * and recursively resolves all variable references.
     * 
     * @param key the property key to retrieve
     * @return the property value with all variables interpolated, or null if not found
     * @throws RuntimeException if properties file cannot be loaded
     */
    public static String getProperty(String key) {
        try {
            if (Objects.isNull(propertiesCache.get(key))) {
                Properties appProps = getProperties();
                String property = appProps.getProperty(key);

                if (property != null) {
                    while (property.contains("{") && property.contains("}")) {
                        String otherProperty = property.substring(property.indexOf("{") + 1, property.indexOf("}"));
                        String otherValue = appProps.getProperty(otherProperty);
                        property = property.replace("{" + otherProperty + "}", otherValue);
                    }

                    propertiesCache.put(key, property);
                }
            }

            return propertiesCache.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Loads the properties file from the classpath.
     * The properties file is determined by the system property {@code app.config.file}
     * or defaults to {@code app-prod.properties}.
     * 
     * @return loaded Properties object
     * @throws IOException if the properties file cannot be read
     */
    @NotNull
    private static Properties getProperties() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = loader.getResource("");
        Objects.requireNonNull(resourceUrl, "Resource URL is null");
        String propFileName = ObjectUtils.defaultIfNull(
                System.getProperty("app.config.file"), "app-prod.properties");
        String rootPath = resourceUrl.getPath();
        Properties appProps = new Properties();
        try (FileInputStream inStream = new FileInputStream(rootPath + propFileName)) {
            appProps.load(inStream);
        }
        return appProps;
    }

    /**
     * Retrieves a typed property value with a default fallback.
     * This method performs automatic type conversion based on the default value's type
     * and returns the default if the property is not found or cannot be converted.
     * 
     * @param <T> the expected property type
     * @param key the property key to retrieve
     * @param defaultValue the default value to return if property is missing or invalid
     * @return the typed property value or the default value
     */
    public static <T> T getProperty(String key, T defaultValue) {
        String property = getProperty(key);
        T value = parseProperty(property, defaultValue);
        return ObjectUtils.defaultIfNull(value, defaultValue);
    }
    
    /**
     * Parses a string property value to the specified type based on the default value's type.
     * Supports automatic conversion for common types including collections.
     * 
     * @param <T> the target type
     * @param property the string property value to parse
     * @param defaultValue the default value used for type inference
     * @return the parsed value of the target type, or default value if parsing fails
     */
    @SuppressWarnings("unchecked")
    private static <T> T parseProperty(String property, T defaultValue) {
        if (property == null) return defaultValue;
        try {
            T object = null;
            if (defaultValue instanceof String) {
                object = (T) property;
            } else if (defaultValue instanceof Integer) {
                object = (T) Integer.valueOf(property);
            } else if (defaultValue instanceof Long) {
                object = (T) Long.valueOf(property);
            } else if (defaultValue instanceof Boolean) {
                object = (T) Boolean.valueOf(property);
            } else if (defaultValue instanceof Double) {
                object = (T) Double.valueOf(property);
            } else if (defaultValue instanceof Collection<?> collection) {
                object = (T) getPropertyCollection(property, collection);
            }
            return object;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Parses a comma-separated string property into a collection.
     * Trims whitespace from individual values and converts each element
     * to the collection's element type.
     * 
     * @param <T> the collection element type
     * @param property the comma-separated property value
     * @param defaultValue the default collection used for type inference
     * @return collection of parsed elements, or default collection if parsing fails
     */
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> getPropertyCollection(String property, Collection<T> defaultValue) {
        String[] split = property.split(",");
        if (split.length == 0) return defaultValue;
        String[] trimmed = Arrays.stream(split).map(String::trim).toArray(String[]::new);
        T[] array = (T[]) new Object[trimmed.length];
        for (int i = 0; i < trimmed.length; i++) {
            array[i] = parseProperty(trimmed[i], defaultValue.iterator().next());
        }
        return List.of(array);
    }
}
