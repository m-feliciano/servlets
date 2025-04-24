package com.dev.servlet.core.util;
import lombok.NoArgsConstructor;
import java.util.Collection;

/**
 * Utility class providing common collection and array manipulation methods.
 * This class offers convenient methods for checking emptiness and performing
 * null-safe operations on collections and arrays.
 * 
 * <p>All methods in this class are static and the class cannot be instantiated.
 * The utility provides consistent null-safe behavior across different collection types.
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * List<String> list = Arrays.asList("item1", "item2");
 * boolean empty = CollectionUtils.isEmpty(list); // false
 * 
 * String[] array = {"a", "b", "c"};
 * boolean arrayEmpty = CollectionUtils.isEmpty(array); // false
 * 
 * // Null-safe operations
 * boolean nullListEmpty = CollectionUtils.isEmpty((List<String>) null); // true
 * boolean nullArrayEmpty = CollectionUtils.isEmpty((String[]) null); // true
 * }
 * </pre>
 * 
 * @since 1.0
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CollectionUtils {
    
    /**
     * Checks if a collection is null or empty.
     * This method provides null-safe checking for any collection type.
     * 
     * @param array the collection to check
     * @return true if the collection is null or has size 0, false otherwise
     */
    public static boolean isEmpty(Collection<?> array) {
        return array == null || array.size() == 0;
    }

    /**
     * Checks if an array is null or empty.
     * This method provides null-safe checking for any array type.
     * 
     * @param <T> the type of array elements
     * @param array the array to check
     * @return true if the array is null or has length 0, false otherwise
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }
}
