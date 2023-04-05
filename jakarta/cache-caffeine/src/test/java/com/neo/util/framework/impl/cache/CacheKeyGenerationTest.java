package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheKeyGenerator;
import com.neo.util.framework.api.cache.spi.CompositeCacheKey;
import com.neo.util.framework.api.cache.spi.UndefinedCacheKeyGenerator;
import com.neo.util.framework.caffeine.impl.CaffeineCache;
import com.neo.util.framework.caffeine.impl.CaffeineCacheConfig;
import com.neo.util.framework.impl.cache.spi.CacheInterceptor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheKeyGenerationTest {

    private static final TestCacheInterceptor TEST_CACHE_INTERCEPTOR = new TestCacheInterceptor();

    @Test
    void defaultKeySTest() {
        // We need a CaffeineCache instance to test the default key logic.
        String cacheName = "test-cache";

        Cache cache = new CaffeineCache(new CaffeineCacheConfig(cacheName, Optional.empty(),Optional.empty(),Optional.empty()));

        Object actualKey = getCacheKey(cache, Collections.emptyList(), new Object[] {});
        assertEquals(cacheName, actualKey);
    }

    @Test
    void explicitSimpleKeyTest() {
        Object expectedKey = new Object();
        Object actualKey = getCacheKey(List.of((short) 1), new Object[] { new Object(), expectedKey });
        // A cache key with one element should be the element itself (same object reference).
        assertEquals(expectedKey, actualKey);
    }

    @Test
    void explicitCompositeKeyTest() {
        Object keyElement1 = new Object();
        Object keyElement2 = new Object();
        Object expectedKey = new CompositeCacheKey(keyElement1, keyElement2);
        Object actualKey = getCacheKey(List.of((short) 0, (short) 2),
                new Object[] { keyElement1, new Object(), keyElement2 });
        assertEquals(expectedKey, actualKey);
    }

    @Test
    void implicitSimpleKeyTest() {
        Object expectedKey = new Object();
        Object actualKey = getCacheKey(Collections.emptyList(), new Object[] { expectedKey });
        // A cache key with one element should be the element itself (same object reference).
        assertEquals(expectedKey, actualKey);
    }

    @Test
    void implicitCompositeKeyTest() {
        Object keyElement1 = new Object();
        Object keyElement2 = new Object();
        Object expectedKey = new CompositeCacheKey(keyElement1, keyElement2);
        Object actualKey = getCacheKey(Collections.emptyList(), new Object[] { keyElement1, keyElement2 });
        assertEquals(expectedKey, actualKey);
    }

    private Object getCacheKey(Cache cache, List<Short> cacheKeyParameterPositions, Object[] methodParameterValues) {
        return TEST_CACHE_INTERCEPTOR.getCacheKey(cache, UndefinedCacheKeyGenerator.class, cacheKeyParameterPositions, null,
                methodParameterValues);
    }

    private Object getCacheKey(List<Short> cacheKeyParameterPositions, Object[] methodParameterValues) {
        return TEST_CACHE_INTERCEPTOR.getCacheKey(null, UndefinedCacheKeyGenerator.class, cacheKeyParameterPositions, null,
                methodParameterValues);
    }

    // This inner class changes the CacheInterceptor#getCacheKey method visibility to public.
    private static class TestCacheInterceptor extends CacheInterceptor {
        @Override
        public Object getCacheKey(Cache cache, Class<? extends CacheKeyGenerator> keyGeneratorClass,
                List<Short> cacheKeyParameterPositions, Method method, Object[] methodParameterValues) {
            return super.getCacheKey(cache, keyGeneratorClass, cacheKeyParameterPositions, method, methodParameterValues);
        }
    }
}
