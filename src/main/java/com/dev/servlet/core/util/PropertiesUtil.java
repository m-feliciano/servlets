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

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PropertiesUtil {

    /**
     * Gets property.
     *
     * @param key from the {@linkplain Properties} file
     * @return the property
     */
    public static String getProperty(String key) {
        try {
            Properties appProps = getProperties();
            String property = appProps.getProperty(key);

            while (property != null && property.contains("{") && property.contains("}")) {
                String otherProperty = property.substring(property.indexOf("{") + 1, property.indexOf("}"));
                String otherValue = appProps.getProperty(otherProperty);
                property = property.replace("{" + otherProperty + "}", otherValue);
            }
            return property;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
     * Gets property.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the property
     */
    public static <T> T getProperty(String key, T defaultValue) {
        String property = getProperty(key);
        T value = parseProperty(property, defaultValue);
        return ObjectUtils.defaultIfNull(value, defaultValue);
    }

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
     * Gets property responseData.
     *
     * @param property     the property
     * @param defaultValue the default value
     * @return the property responseData
     */
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> getPropertyCollection(String property, Collection<T> defaultValue) {
        // if its a responseData, parse the responseData with the default value type
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
