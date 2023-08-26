package com.neo.util.framework.api.cache;

import com.neo.util.framework.api.cache.spi.CacheName;

import java.util.Optional;
import java.util.Set;

/**
 * This interface can be used to retrieve all existing {@link Cache} objects
 * The {@link CacheName} annotation can also be used to inject and access a specific cache from its name.
 */
public interface CacheManager {

    /**
     * Gets a collection of all cache names.
     *
     * @return names of all caches
     */
    Set<String> getCacheNames();

    /**
     * Gets the cache identified by the given name.
     *
     * @param name cache name
     * @return an {@link Optional} containing the identified cache if it exists, or an empty {@link Optional} otherwise
     */
    Optional<Cache> getCache(String name);

    /**
     * Reloads the config.
     */
    void reload();
}
