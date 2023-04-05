package com.neo.util.framework.impl.cache.spi;

import com.neo.util.framework.api.cache.spi.CacheKeyParameterPositions;

import java.util.Collections;
import java.util.List;

/**
 * All cache annotations for the current context
 *
 * @param interceptorBindings list cache annotations
 * @param cacheKeyParameterPositions key positions provided by {@link CacheKeyParameterPositions}
 * @param <T> the cache interceptor annotation
 */
public record CacheInterceptionContext<T>(List<T> interceptorBindings, List<Short> cacheKeyParameterPositions) {

    public CacheInterceptionContext(List<T> interceptorBindings, List<Short> cacheKeyParameterPositions) {
        this.interceptorBindings = Collections.unmodifiableList(interceptorBindings);
        this.cacheKeyParameterPositions = Collections.unmodifiableList(cacheKeyParameterPositions);
    }
}
