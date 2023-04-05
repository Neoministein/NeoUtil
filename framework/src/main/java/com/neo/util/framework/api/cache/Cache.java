package com.neo.util.framework.api.cache;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Defines the functionality to cache objects
 */
public interface Cache {

    /**
     * Returns the cache name.
     *
     * @return cache name
     */
    String getName();

    /**
     * Returns the cached object based on the key
     *
     * @param key of the cached object
     *
     * @return optional of the cached object
     */
    Optional<Object> get(Object key);

    /**
     * Returns a lazy asynchronous action that will emit the cache value based on the key, obtaining that value from
     * {@code valueLoader} if necessary.
     *
     * @param <K> type of the key
     * @param <V> type of the value
     * @param key the key itself
     * @param valueLoader returns the object to be cached when the key doesn't exist
     *
     * @return a lazy asynchronous action that will emit a cache value
     */
    <K, V> CompletableFuture<Object> getAsync(K key, Function<K, V> valueLoader);

    /**
     * Store the value in the cache
     *
     * @param key the key of the value
     * @param value the value to cache itself
     */
    void put(Object key, Object value);

    /**
     * Stores a map of values in the cache
     *
     * @param map data to store
     */
    void putAll(Map<Object, Object> map);

    /**
     * Invalidates the object behind the provided key
     *
     * @param key of the object to invalidate
     */
    void invalidate(Object key);

    /**
     * Removes the cache entry based on key from the cache.
     *
     * @param key to remove
     *
     * @return lazy asynchronous action which validates the removal
     */
    CompletableFuture<Void> invalidateAsync(Object key);

    /**
     * Invalidates the given objects behind the keys
     *
     * @param keys of the objects to invalidate
     */
    void invalidateAll(Iterable<Object> keys);

    /**
     * Flushes the cache
     */
    void invalidateAll();

    /**
     * Removes all entries from the cache.
     *
     * @return lazy asynchronous action which validates the removal
     */
    CompletableFuture<Void> invalidateAllAsync();

    /**
     * Get keys which are currently cached
     */
    Set<Object> getAllKeys();
}
