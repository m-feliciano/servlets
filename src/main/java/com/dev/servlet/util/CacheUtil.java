package com.dev.servlet.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for managing cache entries.
 *
 * @since 1.0.0
 */
public final class CacheUtil {

    private static final Map<String, Map<String, CacheEntry<?>>> IN_MEMORY_CACHE = new ConcurrentHashMap<>();

    private static final long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(
            PropertiesUtil.getProperty("cache.expiration.time", 1440L) // 1 day
    );

    // ScheduledExecutorService to clear expired entries
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        // Schedule a task to clear expired entries every EXPIRATION_TIME milliseconds
        scheduler.scheduleAtFixedRate(
                CacheUtil::clearExpiredEntries,
                EXPIRATION_TIME, EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * Cache entry (holds the responseData and timestamp).
     *
     * @param data
     * @param timestamp
     */
    private record CacheEntry<T>(Collection<T> data, long timestamp) {
        // Empty record
    }

    private CacheUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Set the cached data.
     *
     * @param key        the key
     * @param token      the token
     * @param collection the collection to cache
     */
    public static <T extends Serializable> void set(String key, String token, Collection<T> collection) {
        init(token);

        String cacheKey = getCacheKey(key, token);
        CacheEntry<T> entry = new CacheEntry<>(collection, System.currentTimeMillis());
        getHash(token).put(cacheKey, entry);
    }

    /**
     * Get the cached data.
     *
     * @param key   the key
     * @param token the token
     * @return the cached data
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> get(String key, String token) {
        init(token);

        String cacheKey = getCacheKey(key, token);
        CacheEntry<T> entry = (CacheEntry<T>) getHash(token).get(cacheKey);
        if (entry != null && !isExpired(entry)) {
            List<T> data = (List<T>) entry.data();
            return Collections.unmodifiableList(data);
        }

        return Collections.emptyList();
    }

    /**
     * Initialize the cache for the given token.
     *
     * @param token the token
     */
    private static void init(String token) {
        IN_MEMORY_CACHE.computeIfAbsent(token, k -> new ConcurrentHashMap<>());
    }

    public static void clear(String key, String token) {
        String cacheKey = getCacheKey(key, token);
        getHash(token).remove(cacheKey);
    }

    public static void clearAll(String token) {
        if (token != null) {
            IN_MEMORY_CACHE.remove(token);
        }
    }

    private static Map<String, CacheEntry<?>> getHash(String token) {
        return IN_MEMORY_CACHE.get(token);
    }

    private static String getCacheKey(String key, String token) {
        return "%s_%s".formatted(key, token);
    }

    private static boolean isExpired(CacheEntry<?> entry) {
        return System.currentTimeMillis() - entry.timestamp() > EXPIRATION_TIME;
    }

    private static void clearExpiredEntries() {
        for (Map<String, CacheEntry<?>> tokenCache : IN_MEMORY_CACHE.values()) {
            tokenCache.values().removeIf(CacheUtil::isExpired);
        }
    }
}