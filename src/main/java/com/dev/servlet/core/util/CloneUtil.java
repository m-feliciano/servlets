package com.dev.servlet.core.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for deep cloning objects and collections using JSON serialization.
 * This class provides thread-safe deep cloning capabilities by converting objects
 * to JSON and back, effectively creating completely independent copies.
 * 
 * <p>The cloning mechanism:
 * <ul>
 *   <li>Uses Jackson ObjectMapper for JSON serialization/deserialization</li>
 *   <li>Handles Hibernate lazy loading artifacts automatically</li>
 *   <li>Ignores unknown properties during deserialization</li>
 *   <li>Provides null-safe operations</li>
 * </ul>
 * 
 * <p>This approach is particularly useful for:
 * <ul>
 *   <li>Cache isolation - preventing cache pollution</li>
 *   <li>Defensive copying of mutable objects</li>
 *   <li>Creating independent object graphs</li>
 *   <li>Breaking reference sharing between layers</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Clone a single object
 * User original = new User("John", "john@example.com");
 * User clone = CloneUtil.forceClone(original);
 * clone.setName("Jane"); // Original remains unchanged
 * 
 * // Clone a collection
 * List<Product> products = getProducts();
 * List<Product> clonedProducts = CloneUtil.cloneList(products);
 * 
 * // JSON conversion utilities
 * String json = CloneUtil.toJson(user);
 * User restored = CloneUtil.fromJson(json, User.class);
 * }
 * </pre>
 * 
 * <p><strong>Performance Note:</strong> JSON-based cloning is more expensive than
 * shallow copying but provides complete object independence. Use appropriately
 * based on your use case requirements.
 * 
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CloneUtil {

    /**
     * Mixin class to ignore Hibernate proxy artifacts during JSON serialization.
     * This prevents issues with lazy loading proxies that contain non-serializable fields.
     */
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private abstract static class IgnoreHibernateMixin {
    }

    /**
     * Pre-configured ObjectMapper with settings optimized for object cloning.
     * - Ignores unknown properties to handle version compatibility
     * - Includes mixin to handle Hibernate proxies
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
            .addMixIn(Object.class, IgnoreHibernateMixin.class);

    /**
     * Creates a deep clone of an object using JSON serialization.
     * This method creates a completely independent copy of the object,
     * breaking all reference sharing.
     * 
     * @param <T> the type of object to clone
     * @param object the object to clone
     * @return a deep clone of the object, or null if input is null or cloning fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T forceClone(T object) {
        if (object == null) return null;
        String json = toJson(object);
        Class<T> clazz = (Class<T>) object.getClass();
        return fromJson(json, clazz);
    }

    /**
     * Creates a deep clone of a collection, cloning each element individually.
     * The returned list is a new ArrayList containing cloned elements.
     * 
     * @param <T> the type of collection elements
     * @param objects the collection to clone
     * @return a new List containing cloned elements, or empty list if input is null/empty
     */
    public static <T> List<T> cloneList(Collection<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            String json = toJson(objects);
            CollectionType valueType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, objects.iterator().next().getClass());
            return objectMapper.readValue(json, valueType);
        } catch (Exception e) {
            log.error("Error cloning list: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converts an object to its JSON string representation.
     * Uses the configured ObjectMapper with Hibernate proxy handling.
     * 
     * @param object the object to serialize
     * @return JSON string representation, or null if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error serializing object to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Creates an object from its JSON string representation.
     * Uses the configured ObjectMapper with lenient deserialization settings.
     * 
     * @param <T> the target type
     * @param json the JSON string to deserialize
     * @param clazz the target class
     * @return deserialized object, or null if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error deserializing JSON to class {}: {}", clazz.getName(), e.getMessage());
            return null;
        }
    }
}
