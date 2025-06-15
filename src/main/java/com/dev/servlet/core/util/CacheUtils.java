package com.dev.servlet.core.util;


import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public final class CacheUtils {

    private static final CacheManager cacheManager;
    private static final ConcurrentMap<String, Cache<String, Container>> tokenCaches = new ConcurrentHashMap<>();
    private static final long EXPIRATION_MINUTES = TimeUnit.DAYS.toMinutes(1);

    private static final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentMap<String, Long> lastAccessMap = new ConcurrentHashMap<>();
    private static final long CACHE_IDLE_TIMEOUT_MINUTES = 60; // configurable

    static {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        log.info("Ehcache CacheManager initialized");
        // Start cleanup task
        cleaner.scheduleAtFixedRate(
                CacheUtils::cleanupUnusedCaches,
                CACHE_IDLE_TIMEOUT_MINUTES, CACHE_IDLE_TIMEOUT_MINUTES,
                TimeUnit.MINUTES);
    }

    /**
     * Returns the cache for the given token, creating it if it does not exist.
     *
     * @param token the token to identify the cache
     * @return the cache associated with the token
     */
    private static Cache<String, Container> getOrCreateCache(String token) {
        if (tokenCaches.containsKey(token)) {
            return tokenCaches.get(token);
        }

        String cacheName = "cache_" + token;
        cacheManager.removeCache(cacheName); // Ensure no stale cache
        CacheConfigurationBuilder<String, Container> config = cacheConfigurationBuilder();
        cacheManager.createCache(cacheName, config);

        log.info("Created new cache for token: {}", shortTokenForKey(token));
        Cache<String, Container> cache = cacheManager.getCache(cacheName, String.class, Container.class);
        tokenCaches.put(token, cache);

        lastAccessMap.put(token, System.currentTimeMillis());
        return cache;
    }

    /**
     * Creates a new cache configuration with a time-to-live expiration policy.
     *
     * @return the cache configuration builder
     */
    private static CacheConfigurationBuilder<String, Container> cacheConfigurationBuilder() {
        return CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        String.class,
                        Container.class,
                        ResourcePoolsBuilder.heap(1000)
                )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(EXPIRATION_MINUTES)));
    }

    /**
     * Caches a collection of data associated with a key and token.
     *
     * @param key        the key to associate with the data
     * @param token      the token to identify the cache
     * @param collection the collection of data to cache
     * @param <T>        the type of elements in the collection
     */
    public static <T> void set(String key, String token, Collection<T> collection) {
        String shortToken = shortTokenForKey(token);

        getOrCreateCache(shortToken).put(key, new Container(List.copyOf(collection)));
        log.debug("Cached data for key='{}', token='{}'", key, shortTokenForKey(token));
    }

    /**
     * Caches an object associated with a key and token.
     *
     * @param key    the key to associate with the object
     * @param token  the token to identify the cache
     * @param object the object to cache
     * @param <T>    the type of the object
     */
    public static <T> void setObject(String key, String token, T object) {
        String shortToken = shortTokenForKey(token);

        getOrCreateCache(shortToken).put(key, new Container(object));
        log.debug("Cached object for key='{}', token='{}'", key, shortToken);
    }

    /**
     * Retrieves a collection of data associated with a key and token from the cache.
     *
     * @param key   the key to look up
     * @param token the token to identify the cache
     * @param <T>   the type of elements in the collection
     * @return a list of cached data, or an empty list if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> get(String key, String token) {
        String shortToken = shortTokenForKey(token);
        Container value = getOrCreateCache(shortToken).get(key);

        log.debug("Retrieved data for key='{}', token='{}': {}", key, shortToken, value != null ? "HIT" : "MISS");

        if (value != null && value.data() instanceof List) {
            return (List<T>) value.data();

        } else if (value != null && value.data() instanceof Collection) {
            return List.copyOf((Collection<T>) value.data());
        }

        return Collections.emptyList();
    }

    /**
     * Retrieves an object associated with a key and token from the cache.
     *
     * @param key   the key to look up
     * @param token the token to identify the cache
     * @param <T>   the type of the object
     * @return the cached object, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObject(String key, String token) {
        String shortToken = shortTokenForKey(token);

        Container value = getOrCreateCache(shortToken).get(key);
        log.debug("Retrieved data for key='{}', token='{}': {}", key, shortToken, value != null ? "HIT" : "MISS");

        if (value == null) {
            return null;
        }

        return (T) value.data();
    }

    /**
     * Clears a specific cache entry associated with a key and token.
     *
     * @param key   the key to clear
     * @param token the token to identify the cache
     */
    public static void clear(String key, String token) {
        String shortToken = shortTokenForKey(token);
        getOrCreateCache(shortToken).remove(key);
        log.info("Cleared cache entry for key='{}', token='{}'", key, shortToken);
    }

    /**
     * Clears all cache entries associated with a token.
     *
     * @param token the token to identify the cache
     */
    public static void clearAll(String token) {
        String shortToken = shortTokenForKey(token);
        String cacheName = "cache_" + shortToken;
        cacheManager.removeCache(cacheName);
        tokenCaches.remove(shortToken);
        log.info("Cleared all cache entries for token='{}'", shortToken);
    }

    private static String shortTokenForKey(String token) {
        return token.substring(0, 25);
    }

    /**
     * Closes the cache manager and clears all caches.
     */
    // Cleanup method
    private static void cleanupUnusedCaches() {
        long now = System.currentTimeMillis();

        for (var entry : lastAccessMap.entrySet()) {
            if (now - entry.getValue() > TimeUnit.MINUTES.toMillis(CACHE_IDLE_TIMEOUT_MINUTES)) {
                String token = entry.getKey();
                String cacheName = "cache_" + token;
                cacheManager.removeCache(cacheName);
                tokenCaches.remove(token);
                lastAccessMap.remove(token);

                log.info("Evicted unused cache for token='{}'", token);
            }
        }
    }

    // In close(), also shutdown the cleaner
    public static void close() {
        cacheManager.close();
        tokenCaches.clear();
        lastAccessMap.clear();
        cleaner.shutdownNow();
        log.info("CacheManager closed and all caches cleared");
    }

    public static void clearCacheKeyPrefix(String cacheKeyPrefix, String cacheToken) {
        String shortToken = shortTokenForKey(cacheToken);
        Cache<String, Container> cache = tokenCaches.get(shortToken);
        if (cache != null) {
            cache.forEach(entry -> {
                if (entry.getKey().startsWith(cacheKeyPrefix)) {
                    cache.remove(entry.getKey());
                    log.info("Cleared cache entry with prefix '{}' for token='{}'", cacheKeyPrefix, shortToken);
                }
            });
        } else {
            log.warn("No cache found for token='{}' to clear entries with prefix '{}'", shortToken, cacheKeyPrefix);
        }
    }

    private record Container(Object data) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}

