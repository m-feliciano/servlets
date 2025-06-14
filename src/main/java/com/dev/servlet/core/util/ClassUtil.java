package com.dev.servlet.core.util;

import com.dev.servlet.application.dto.records.KeyPair;
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
import java.util.Optional;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ClassUtil {

    /**
     * Load the classes in the package
     *
     * @param packageName
     * @return
     * @throws Exception
     */
    public static List<Class<?>> scanPackage(String packageName) throws Exception {
        return scanPackage(packageName, null);
    }

    /**
     * Load the classes in the package, with the specified annotations
     *
     * @param packageName
     * @param annotations {@linkplain Annotation}
     * @return
     * @throws Exception
     */
    public static List<Class<?>> scanPackage(String packageName, Class<? extends Annotation>[] annotations) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        // Get the files in the package
        File[] files = getFiles(packageName);

        for (File file : files) {
            // Check if the file is a directory
            if (file.isDirectory()) {
                // Load the classes in the subdirectory
                classes.addAll(scanPackage(packageName + "." + file.getName(), annotations));
            } else if (file.getName().endsWith(".class")) {
                // Get the class key
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);

                // Load the class
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
     * Get the files in the package
     *
     * @param packageName
     * @return {@linkplain File}
     * @throws Exception
     */
    private static File[] getFiles(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new Exception("Class loader is null");
        }

        // Get the package path
        String path = packageName.replace('.', File.separatorChar);

        // Get the package directory
        File directory = new File(classLoader.getResource(path).getFile());

        if (!directory.exists()) {
            throw new Exception("Directory does not exist");
        }

        // Get the responseData of files in the directory
        File[] files = directory.listFiles();

        if (files == null) {
            throw new Exception("No files found in the directory");
        }
        return files;
    }

    /**
     * Cast an object to the specified type
     *
     * @param clazz  {@linkplain Class}
     * @param object {@linkplain Object}
     * @param <T>
     * @return {@linkplain T}
     */
    public static <T> T castObject(Class<T> clazz, Object object) {
        return clazz.cast(object);
    }

    /**
     * Cast a list of objects to a list of the specified type
     *
     * @param clazz {@linkplain Class}
     * @param list  {@linkplain Collection}
     * @param <T>
     * @return {@linkplain Collection}
     */
    public static <T> Collection<T> castList(Class<T> clazz, Collection<?> list) {
        List<T> result = new ArrayList<>();
        for (Object object : list) {
            result.add(castObject(clazz, object));
        }
        return result;
    }

    /**
     * Cast a list of objects to a list of the specified type
     *
     * @param list {@linkplain List}
     * @param <T>
     * @return {@linkplain List}
     */
    public static <T> List<T> castList(List<?> list) {
        List<T> result = new ArrayList<>();
        for (Object object : list) {
            result.add((T) object);
        }
        return result;
    }

    /**
     * Convert the value to the specified type
     *
     * @param type  {@linkplain Class} of the type {@linkplain T}
     * @param value {@linkplain Object}
     * @return {@linkplain Object}
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
     * Get the generic type of T (U) where T the superclass and 'U' is the subclass reference
     */
    public static <T> Class<T> getSubClassType(Class<?> clazz) {
        return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Extract the type from the class
     *
     * <p>Example:
     * <pre>{@code
     * class A<C, D> {}
     * }</pre>
     * <p>Extract the type of the class C
     * <pre>{@code
     * Class<C> clazz = extractType(this.getClass(), 1);
     * }</pre>
     *
     * @param clazz    {@linkplain Class}
     * @param position the position of the type
     * @param <U>      {@linkplain Class}
     * @return {@linkplain Class} of the expected type {@linkplain U}
     * @author marcelo.feliciano
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
     * Create an instance of the class
     *
     * @param clazz {@linkplain Class}
     * @param <V>   the type of the class
     * @return {@linkplain Optional} of {@linkplain V}
     * @author marcelo.feliciano
     */
    public static <V> Optional<V> createInstance(Class<V> clazz) {
        try {
            return Optional.of(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Set the field value
     *
     * @param field  {@linkplain Field}
     * @param entity {@linkplain Object}
     * @param value  {@linkplain Object}
     */
    public static <T> void setFieldValue(Field field, T entity, Object value) {
        try {
            Object wrapper = ClassUtil.castWrapper(field.getType(), value);
            field.set(entity, wrapper);
        } catch (Exception ignored) {

        }
    }

    /**
     * Populate the fields of the object
     *
     * @param object {@linkplain T} the transfer object
     * @param data   {@linkplain List} of {@linkplain KeyPair}
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