package com.dev.servlet.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public final class PropertiesUtil {

    private PropertiesUtil() {
    }

    /**
     * Gets property.
     *
     * @param key from the {@link Properties} file
     * @return the property
     */
    public static String getProperty(String key) {
        try {
            String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            String appConfigPath = rootPath + "app.properties";
//            String catalogConfigPath = rootPath + "catalog";

            Properties appProps = new Properties();
            try (FileInputStream inStream = new FileInputStream(appConfigPath)) {
                appProps.load(inStream);
            }
//            Properties catalogProps = new Properties();
//            catalogProps.load(new FileInputStream(catalogConfigPath));

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
        String[] split = property.split(";");
        if (split.length == 0) return defaultValue;

        String[] trimmed = Arrays.stream(split).map(String::trim).toArray(String[]::new);
        T[] array = (T[]) new Object[trimmed.length];
        for (int i = 0; i < trimmed.length; i++) {
            array[i] = parseProperty(trimmed[i], defaultValue.iterator().next());
        }

        return List.of(array);
    }
}
