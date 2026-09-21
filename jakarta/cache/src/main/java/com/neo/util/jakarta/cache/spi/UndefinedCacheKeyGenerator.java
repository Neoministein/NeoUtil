package com.neo.util.jakarta.cache.spi;

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
