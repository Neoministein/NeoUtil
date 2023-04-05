package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.spi.CacheResult;
import com.neo.util.framework.impl.cache.spi.CacheResultInterceptor;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.List;


@ExtendWith(WeldJunit5Extension.class)
@AddEnabledInterceptors(CacheResultInterceptor.class)
class ThrowExecutionExceptionCauseIT extends AbstractCacheIT {

    private static final String FORCED_EXCEPTION_MESSAGE = "Forced exception";

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
    void runtimeExceptionThrowDuringCacheComputationTest() {
        NumberFormatException e = Assertions.assertThrows(NumberFormatException.class, () -> {
            subject.throwRuntimeExceptionDuringCacheComputation();
        });
        Assertions.assertEquals(FORCED_EXCEPTION_MESSAGE, e.getMessage());
        // Let's check we didn't put an uncompleted future in the cache because of the previous exception.
        Assertions.assertThrows(NumberFormatException.class, () -> {
            subject.throwRuntimeExceptionDuringCacheComputation();
        });

        Assertions.assertEquals(1, subject.runtimeExceptionCalls);
    }

    @Test
    void checkedExceptionThrowDuringCacheComputationTest() {
        IOException e = Assertions.assertThrows(IOException.class, () -> {
            subject.throwCheckedExceptionDuringCacheComputation();
        });
        Assertions.assertEquals(FORCED_EXCEPTION_MESSAGE, e.getMessage());

        Assertions.assertThrows(IOException.class, () -> {
            subject.throwCheckedExceptionDuringCacheComputation();
        });

        Assertions.assertEquals(1, subject.checkedExceptionCalls);
    }

    @Test
    void errorThrowDuringCacheComputationTest() {
        OutOfMemoryError e = Assertions.assertThrows(OutOfMemoryError.class, () -> {
            subject.throwErrorDuringCacheComputation();
        });
        Assertions.assertEquals(FORCED_EXCEPTION_MESSAGE, e.getMessage());

        Assertions.assertThrows(OutOfMemoryError.class, () -> {
            subject.throwErrorDuringCacheComputation();
        });

        Assertions.assertEquals(2, subject.outOfMemoryCacheMethodCalls);
    }

    @ApplicationScoped
    static class CachedService {

        protected static int outOfMemoryCacheMethodCalls = 0;
        protected static int runtimeExceptionCalls = 0;
        protected static int checkedExceptionCalls = 0;

        @CacheResult(cacheName = "runtime-exception-cache")
        public String throwRuntimeExceptionDuringCacheComputation() {
            runtimeExceptionCalls++;
            throw new NumberFormatException(FORCED_EXCEPTION_MESSAGE);
        }

        @CacheResult(cacheName = "checked-exception-cache")
        public String throwCheckedExceptionDuringCacheComputation() throws IOException {
            checkedExceptionCalls++;
            throw new IOException(FORCED_EXCEPTION_MESSAGE);
        }

        @CacheResult(cacheName = "error-cache")
        public String throwErrorDuringCacheComputation() {
            outOfMemoryCacheMethodCalls++;
            throw new OutOfMemoryError(FORCED_EXCEPTION_MESSAGE);
        }

    }
}
