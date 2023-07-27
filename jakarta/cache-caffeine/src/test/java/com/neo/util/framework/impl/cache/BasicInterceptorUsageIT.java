package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.spi.*;
import com.neo.util.framework.impl.cache.spi.CacheInvalidateAllInterceptor;
import com.neo.util.framework.impl.cache.spi.CacheInvalidateInterceptor;
import com.neo.util.framework.impl.cache.spi.CachePutInterceptor;
import com.neo.util.framework.impl.cache.spi.CacheResultInterceptor;
import jakarta.inject.Singleton;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/**
 * Tests a cache with a <b>default</b> cache key.
 */
@ExtendWith(WeldJunit5Extension.class)
@AddEnabledInterceptors({CacheResultInterceptor.class, CachePutInterceptor.class, CacheInvalidateInterceptor.class, CacheInvalidateAllInterceptor.class})
class BasicInterceptorUsageIT extends AbstractCacheIT {

    private static final Object KEY = new Object();

    CachedService subject;

    @BeforeEach
    void before() {
        super.setupConfig();
        subject = weld.select(CachedService.class).get();
    }

    @Override
    protected List<Class<?>> basicCDIClasses() {
        List<Class<?>> classes = super.basicCDIClasses();
        classes.add(CachedService.class);
        classes.add(CachePutInterceptor.class);
        classes.add(CacheResultInterceptor.class);
        classes.add(CacheInvalidateInterceptor.class);
        classes.add(CacheInvalidateAllInterceptor.class);
        return classes;
    }

    @Test
    void testAllCacheAnnotations() {
        // STEP 1
        // Action: no-arg @CacheResult-annotated method call.
        // Expected effect: method invoked and result cached.
        // Verified by: STEP 2.
        Object value1 = subject.cachedMethod();

        // STEP 2
        // Action: same call as STEP 1.
        // Expected effect: method not invoked and result coming from the cache.
        // Verified by: same object reference between STEPS 1 and 2 results.
        Object value2 = subject.cachedMethod();
        Assertions.assertEquals(value1, value2);

        // STEP 3
        // Action: @CacheResult-annotated method call with a key argument.
        // Expected effect: method invoked and result cached.
        // Verified by: different objects references between STEPS 2 and 3 results.
        Object value3 = subject.cachedMethodWithKey(KEY);
        Assertions.assertNotEquals(value2, value3);

        // STEP 4
        // Action: default key cache entry invalidation.
        // Expected effect: STEP 2 cache entry removed.
        // Verified by: STEP 5.
        subject.invalidate("cachedMethod");

        // STEP 5
        // Action: same call as STEP 2.
        // Expected effect: method invoked because of STEP 4 and result cached.
        // Verified by: different objects references between STEPS 2 and 5 results.
        Object value5 = subject.cachedMethod();
        Assertions.assertNotEquals(value2, value5);

        // STEP 6
        // Action: same call as STEP 3.
        // Expected effect: method not invoked and result coming from the cache.
        // Verified by: same object reference between STEPS 3 and 6 results.
        Object value6 = subject.cachedMethodWithKey(KEY);
        Assertions.assertEquals(value3, value6);

        // STEP 7
        // Action: full cache invalidation.
        // Expected effect: empty cache.
        // Verified by: STEPS 8 and 9.
        subject.invalidateAll();

        // STEP 8
        // Action: same call as STEP 5.
        // Expected effect: method invoked because of STEP 7 and result cached.
        // Verified by: different objects references between STEPS 5 and 8 results.
        Object value8 = subject.cachedMethod();
        Assertions.assertNotEquals(value5, value8);

        // STEP 9
        // Action: same call as STEP 6.
        // Expected effect: method invoked because of STEP 7 and result cached.
        // Verified by: different objects references between STEPS 6 and 9 results.
        Object value9 = subject.cachedMethodWithKey(KEY);
        Assertions.assertNotEquals(value6, value9);

        // STEP 10
        // Action: @CachePut-annotated method call with a key and value argument.
        // Expected effect: method invoked and cache gets manually get override by the provided value.
        // Verified by: different objects references between STEPS 6 and 9 results.
        Object value10 = new Object();
        subject.put(KEY, value10);
        Assertions.assertNotEquals(value6, value10);

        // STEP 11
        // Action: @CacheResult-annotated method call with a key argument.
        // Expected effect: method invoked because of STEP 10 and cache is returned.
        // Verified by: same objects references between STEPS 10 and 11 results.
        Object value11 = subject.cachedMethodWithKey(KEY);
        Assertions.assertEquals(value10, value11);
    }

    @Singleton
    static class CachedService {

        private static final String CACHE_NAME = "test-cache";

        @CacheResult(cacheName = CACHE_NAME)
        public Object cachedMethod() {
            return new Object();
        }

        @CacheResult(cacheName = CACHE_NAME)
        public Object cachedMethodWithKey(Object key) {
            return new Object();
        }

        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate(String key) {
        }

        @CacheInvalidateAll(cacheName = CACHE_NAME)
        public void invalidateAll() {
        }

        @CacheKeyParameterPositions(0)
        @CachePut(valueParameterPosition = 1, cacheName = CACHE_NAME)
        public void put(Object key, Object toCache) {
        }
    }
}
