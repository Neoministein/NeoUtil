package com.neo.util.framework.api.cache;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.framework.api.cache.spi.CacheName;

import java.util.Optional;
import java.util.Set;

/**
 * This interface can be used to retrieve all existing {@link Cache} objects
 * The {@link CacheName} annotation can also be used to inject and access a specific cache from its name.
 */
public interface CacheManager {

    String E_CACHE_DOES_NOT_EXIST = "cache/invalid-id";

    ExceptionDetails EX_CACHE_DOES_NOT_EXIST = new ExceptionDetails(E_CACHE_DOES_NOT_EXIST,
            "The cache id [{0}] does not exist.");

    /**
     * Gets a collection of all cache names.
     *
     * @return names of all caches
     */
    Set<String> fetchCacheNames();

    /**
     * Gets the cache identified by the given name.
     *
     * @param name cache name
     * @return an {@link Optional} containing the identified cache if it exists, or an empty {@link Optional} otherwise
     */
    Optional<Cache> fetchCache(String name);

    default Cache requestCache(String name) {
        return fetchCache(name).orElseThrow(() -> new NoContentFoundException(EX_CACHE_DOES_NOT_EXIST, name));
    }

    /**
     * Reloads the config.
     */
    void reload();
}
