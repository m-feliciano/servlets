package com.dev.servlet.pojo.records;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a key pair
 *
 * @since 1.4
 */
public record KeyPair(@Getter String key, @Getter Object value) {

    public static KeyPair of(String name, Object value) {
        return new KeyPair(name, value);
    }

    /**
     * Create a mutable list of key pairs
     *
     * @param name
     * @param value
     * @return
     */
    public static Set<KeyPair> mutableSetOf(String name, Object value) {
        Set<KeyPair> list = new HashSet<>();
        list.add(new KeyPair(name, value));
        return list;
    }

    /**
     * Create an immutable list of key pairs
     *
     * @param name
     * @param value
     * @return
     */
    public static Set<KeyPair> setOf(String name, Object value) {
        return Set.of(new KeyPair(name, value));
    }

    @Override
    public String toString() {
        return "{" + key + ": " + value + "}";
    }
}
