package com.neo.util.framework.api.cache;

import java.util.Map;

/**
 * This class should be implemented by all cache providers to build all cache instances on application startup
 */
public interface CacheBuilder {

    /**
     * Returns all requires instances of {@link Cache} for the application to run correctly
     *
     * @return map of caches
     */
    Map<String, Cache> build();
}
