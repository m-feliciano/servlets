package com.dev.servlet.util;

import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CollectionUtils {

    public static boolean isEmpty(Collection<?> array) {
        return array == null || array.size() == 0;
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }
}
