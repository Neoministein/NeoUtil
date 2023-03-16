package com.neo.util.framework.rest.cache.api;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Defines the functionality to cache objects
 */
public interface ServerCacheService {

    /**
     * Returns the cached object based on the key
     *
     * @param key of the cached object
     * @return optional of the cached object
     */
    Optional<CachedObject> get(String key);

    /**
     * Returns the cached object based on the key and validated that it isn't older than the duration
     *
     * @param key of the cached object
     * @param maxAge to check against the insertion time
     * @return optional of the cached object
     */
    Optional<CachedObject> getMaxAge(String key, Duration maxAge);

    /**
     * Returns the cached object based on the key and validated that it isn't older than the duration
     *
     * @param key of the cached object
     * @param timeUnit the timeunit of the maxAge value
     * @param maxAge to check against the insertion time
     * @return optional of the cached object
     */
    Optional<CachedObject> getMaxAge(String key, TimeUnit timeUnit, long maxAge);

    /**
     * Store the value in the cache
     *
     * @param key the key of the value
     * @param value the value to cache itself
     */
    void put(String key, CachedObject value);

    /**
     * Stores a map of values in the cache
     *
     * @param map data to store
     */
    void putAll(Map<String, CachedObject> map);

    /**
     * Invalidates the object behind the provided key
     * @param key of the object to invalidate
     */
    void invalidate(String key);

    /**
     * Invalidates the given objects behind the keys
     *
     * @param keys of the objects to invalidate
     */
    void invalidateAll(Iterable<String> keys);

    /**
     * Flushes the cache
     */
    void invalidateAll();
}
