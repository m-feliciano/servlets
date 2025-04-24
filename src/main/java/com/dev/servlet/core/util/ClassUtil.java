package com.dev.servlet.core.util;
import com.dev.servlet.domain.transfer.records.KeyPair;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Comprehensive utility class for advanced Java reflection and class manipulation operations.
 * This class provides type-safe methods for package scanning, dynamic casting, object instantiation,
 * and field manipulation using reflection.
 * 
 * <p>Key capabilities:
 * <ul>
 *   <li><strong>Package scanning:</strong> Discover classes in packages with optional annotation filtering</li>
 *   <li><strong>Type-safe casting:</strong> Safe casting with wrapper type conversion</li>
 *   <li><strong>Generic type extraction:</strong> Extract parameterized type information</li>
 *   <li><strong>Dynamic instantiation:</strong> Create instances with exception handling</li>
 *   <li><strong>Field manipulation:</strong> Set field values with automatic type conversion</li>
 *   <li><strong>Object population:</strong> Fill objects from key-value data</li>
 * </ul>
 * 
 * <p>The utility handles common wrapper types automatically:
 * <ul>
 *   <li>String, Integer, Long, Double, Float, Boolean</li>
 *   <li>BigDecimal (with currency parsing)</li>
 *   <li>Date (with formatted parsing)</li>
 *   <li>Custom objects through casting</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Package scanning
 * List<Class<?>> controllers = ClassUtil.scanPackage("com.dev.servlet.controller");
 * List<Class<?>> annotated = ClassUtil.scanPackage("com.dev.servlet.service", 
 *     new Class[]{@Controller.class});
 * 
 * // Type-safe casting
 * List<String> strings = ClassUtil.castList(String.class, objectList);
 * Integer number = ClassUtil.castWrapper(Integer.class, "123");
 * 
 * // Generic type extraction
 * Class<User> userType = ClassUtil.getSubClassType(UserService.class);
 * Class<Product> entityType = ClassUtil.extractType(Repository.class, 1);
 * 
 * // Dynamic instantiation
 * Optional<UserService> service = ClassUtil.createInstance(UserService.class);
 * 
 * // Object population
 * User user = new User();
 * List<KeyPair> data = List.of(new KeyPair("name", "John"), new KeyPair("age", "30"));
 * ClassUtil.fillObject(user, data);
 * }
 * </pre>
 * 
 * <p><strong>Performance Note:</strong> Reflection operations are inherently slower than direct access.
 * Consider caching results for frequently accessed type information.
 * 
 * @since 1.0
 * @see FormatterUtil
 * @see KeyPair
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ClassUtil {
    
    /**
     * Scans a package for all classes without annotation filtering.
     * 
     * @param packageName the package name to scan (e.g., "com.example.service")
     * @return list of all classes found in the package and its subpackages
     * @throws Exception if package scanning fails or package doesn't exist
     */
    public static List<Class<?>> scanPackage(String packageName) throws Exception {
        return scanPackage(packageName, null);
    }

    /**
     * Scans a package for classes with optional annotation filtering.
     * This method recursively scans the package hierarchy and optionally filters
     * classes based on the presence of specific annotations.
     * 
     * @param packageName the package name to scan
     * @param annotations optional array of annotations to filter by (null for no filtering)
     * @return list of classes, filtered by annotations if specified
     * @throws Exception if package scanning fails or package doesn't exist
     */
    public static List<Class<?>> scanPackage(String packageName, Class<? extends Annotation>[] annotations) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = getFiles(packageName);
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(scanPackage(packageName + "." + file.getName(), annotations));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = ClassUtils.getClass(className);
                if (annotations == null) {
                    classes.add(clazz);
                } else {
                    for (var annotation : annotations) {
                        if (clazz.isAnnotationPresent(annotation)) {
                            classes.add(clazz);
                            break;
                        }
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Gets the files in a package directory for scanning.
     * 
     * @param packageName the package name
     * @return array of files in the package directory
     * @throws Exception if directory doesn't exist or can't be accessed
     */
    private static File[] getFiles(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Objects.requireNonNull(classLoader);
        String path = packageName.replace('.', File.separatorChar);
        File directory = new File(classLoader.getResource(path).getFile());
        if (!directory.exists()) {
            throw new Exception("Directory does not exist");
        }
        File[] files = directory.listFiles();
        Objects.requireNonNull(files);
        return files;
    }

    /**
     * Safely casts an object to the specified type.
     * Uses Class.cast() for type-safe casting with ClassCastException handling.
     * 
     * @param <T> the target type
     * @param clazz the target class
     * @param object the object to cast
     * @return the cast object
     * @throws ClassCastException if the object cannot be cast to the target type
     */
    public static <T> T castObject(Class<T> clazz, Object object) {
        return clazz.cast(object);
    }

    /**
     * Casts a collection of objects to a collection of the specified type.
     * Each element is individually cast to the target type.
     * 
     * @param <T> the target element type
     * @param clazz the target element class
     * @param list the collection to cast
     * @return new collection with cast elements
     * @throws ClassCastException if any element cannot be cast to the target type
     */
    public static <T> Collection<T> castList(Class<T> clazz, Collection<?> list) {
        List<T> result = new ArrayList<>();
        for (Object object : list) {
            result.add(castObject(clazz, object));
        }
        return result;
    }

    /**
     * Performs an unchecked generic cast of a list.
     * This method suppresses generic type warnings and should be used carefully.
     * 
     * @param <T> the target element type
     * @param list the list to cast
     * @return the list cast to the target generic type
     */
    public static <T> List<T> castList(List<?> list) {
        List<T> result = new ArrayList<>();
        for (Object object : list) {
            result.add((T) object);
        }
        return result;
    }

    /**
     * Converts and casts values to wrapper types with automatic type conversion.
     * Supports common wrapper types and uses FormatterUtil for specialized parsing.
     * 
     * @param <T> the target wrapper type
     * @param type the target wrapper class
     * @param value the value to convert and cast
     * @return converted value of the target type, or null if conversion fails
     */
    public static <T> T castWrapper(Class<T> type, Object value) {
        if (value == null) return null;
        try {
            if (type == String.class) {
                value = value.toString();
            } else if (type == Integer.class) {
                value = Integer.parseInt(value.toString());
            } else if (type == Long.class) {
                value = Long.parseLong(value.toString());
            } else if (type == Double.class) {
                value = Double.parseDouble(value.toString());
            } else if (type == Float.class) {
                value = Float.parseFloat(value.toString());
            } else if (type == Boolean.class) {
                value = Boolean.parseBoolean(value.toString());
            } else if (type == BigDecimal.class) {
                value = FormatterUtil.parseCurrency(value.toString());
            } else if (type == Date.class) {
                value = FormatterUtil.toDate(value.toString());
            }
            return type.cast(value);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Extracts the generic type parameter from a class's superclass.
     * Useful for getting the entity type from generic repository classes.
     * 
     * @param <T> the extracted type
     * @param clazz the class to extract the type from
     * @return the first generic type parameter of the superclass
     * @throws IllegalArgumentException if the class doesn't have parameterized superclass
     */
    public static <T> Class<T> getSubClassType(Class<?> clazz) {
        return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Extracts a specific generic type parameter by position from a class.
     * Supports multiple generic parameters with position-based access.
     * 
     * @param <U> the extracted type
     * @param clazz the class to extract the type from
     * @param position the position of the type parameter (1-based)
     * @return the generic type parameter at the specified position
     * @throws IllegalArgumentException if the class doesn't have parameterized types
     */
    public static <U> Class<U> extractType(Class<?> clazz, int position) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return (Class<U>) actualTypeArguments[position - 1];
        }
        throw new IllegalArgumentException("Class does not have parameterized types");
    }

    /**
     * Creates a new instance of the specified class using the default constructor.
     * Handles exceptions gracefully by returning an Optional.
     * 
     * @param <V> the type to instantiate
     * @param clazz the class to instantiate
     * @return Optional containing the new instance, or empty if instantiation fails
     */
    public static <V> Optional<V> createInstance(Class<V> clazz) {
        try {
            return Optional.of(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Sets a field value with automatic type conversion.
     * Uses castWrapper to convert the value to the field's type before setting.
     * 
     * @param <T> the entity type
     * @param field the field to set
     * @param entity the entity instance
     * @param value the value to set (will be converted to field type)
     */
    public static <T> void setFieldValue(Field field, T entity, Object value) {
        try {
            Object wrapper = ClassUtil.castWrapper(field.getType(), value);
            field.set(entity, wrapper);
        } catch (Exception ignored) {
        }
    }

    /**
     * Populates an object's fields from a list of key-value pairs.
     * This method automatically matches field names with keys and performs
     * type conversion for compatible values. Only non-static fields that don't
     * belong to the application package are considered.
     * 
     * @param <T> the object type
     * @param object the object to populate
     * @param data the key-value pairs containing field values
     */
    public static <T> void fillObject(T object, List<KeyPair> data) {
        List<Field> fields = FieldUtils.getAllFieldsList(object.getClass())
                .stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.getType().getName().startsWith("com.dev.servlet"))
                .toList();
        List<KeyPair> keyPairs = data.stream()
                .filter(pair -> fields.stream().anyMatch(field -> field.getName().equals(pair.getKey())))
                .toList();
        if (keyPairs.isEmpty()) {
            return;
        }
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                keyPairs.stream()
                        .filter(kp -> kp.getKey().equals(field.getName()))
                        .findFirst()
                        .ifPresent(f -> setFieldValue(field, object, f.getValue()));
            } finally {
                field.setAccessible(false);
            }
        }
    }
}
