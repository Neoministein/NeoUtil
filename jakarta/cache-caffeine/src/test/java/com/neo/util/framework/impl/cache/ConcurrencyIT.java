package com.neo.util.framework.impl.cache;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.framework.api.cache.spi.CacheResult;
import com.neo.util.framework.impl.cache.spi.CacheResultInterceptor;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(WeldJunit5Extension.class)
@AddEnabledInterceptors(CacheResultInterceptor.class)
class ConcurrencyIT extends AbstractCacheIT {

    private static final Object CACHE_KEY = new Object();

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
        classes.add(CacheResultInterceptor.class);
        return classes;
    }

    @Test
    void concurrentCacheAccessTest() throws InterruptedException, ExecutionException {
        // This is required to make sure the CompletableFuture are executed concurrently.
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicReference<String> callingThreadName1 = new AtomicReference<>();
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            callingThreadName1.set(Thread.currentThread().getName());

            return subject.cachedMethodWithoutLockTimeout(CACHE_KEY);
        }, executorService);

        AtomicReference<String> callingThreadName2 = new AtomicReference<>();
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            callingThreadName2.set(Thread.currentThread().getName());

            return subject.cachedMethodWithoutLockTimeout(CACHE_KEY);
        }, executorService);

        // Wait for both futures to complete before running assertions.
        CompletableFuture.allOf(future1, future2).get();

        Assertions.assertEquals(future1.get(), future2.get());

        /*
         * The value loader execution should be done synchronously on the calling thread.
         * Both thread names need to be checked because there's no way to determine which future will be run first.
         */
        Assertions.assertTrue(callingThreadName1.get().equals(future1.get()) || callingThreadName2.get().equals(future1.get()));
    }


    static class CachedService {

        private static final String CACHE_NAME = "test-cache";

        @CacheResult(cacheName = CACHE_NAME)
        public String cachedMethodWithoutLockTimeout(Object key) {
            ThreadUtils.simpleSleep(500);
            return Thread.currentThread().getName();
        }
    }
}
