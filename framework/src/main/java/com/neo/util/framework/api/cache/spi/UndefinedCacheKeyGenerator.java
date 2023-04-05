package com.neo.util.framework.api.cache.spi;

import com.neo.util.framework.api.cache.CacheKeyGenerator;

import java.lang.reflect.Method;

/**
 * This {@link CacheKeyGenerator} should be ignored.
 */
public class UndefinedCacheKeyGenerator implements CacheKeyGenerator {

    @Override
    public Object generate(Method method, Object... methodParams) {
        throw new UnsupportedOperationException("This cache key generator should never be invoked");
    }
}
