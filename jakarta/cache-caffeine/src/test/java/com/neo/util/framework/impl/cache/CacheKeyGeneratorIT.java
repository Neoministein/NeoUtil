package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.CacheKeyGenerator;
import com.neo.util.framework.api.cache.spi.CacheInvalidate;
import com.neo.util.framework.api.cache.spi.CacheKeyParameterPositions;
import com.neo.util.framework.api.cache.spi.CacheResult;
import com.neo.util.framework.api.cache.spi.CompositeCacheKey;
import com.neo.util.framework.impl.cache.spi.CacheInvalidateAllInterceptor;
import com.neo.util.framework.impl.cache.spi.CacheInvalidateInterceptor;
import com.neo.util.framework.impl.cache.spi.CacheResultInterceptor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WeldJunit5Extension.class)
@AddEnabledInterceptors({CacheResultInterceptor.class, CacheInvalidateInterceptor.class, CacheInvalidateAllInterceptor.class})
class CacheKeyGeneratorIT extends AbstractCacheIT{

    private static final String ASPARAGUS = "asparagus";
    private static final String CAULIFLOWER = "cauliflower";
    private static final Object OBJECT = new Object();

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
        classes.add(CacheInvalidateInterceptor.class);
        classes.add(CacheInvalidateAllInterceptor.class);
        classes.add(ApplicationScopedKeyGen.class);
        classes.add(RequestScopedKeyGen.class);
        classes.add(DependentKeyGen.class);
        return classes;
    }

    @Test
    @ActivateRequestContext
    void testAllCacheKeyGeneratorKinds() {
        BigInteger value3 = subject.cachedMethod2();
        BigInteger value4 = subject.cachedMethod2();
        assertSame(value3, value4);

        subject.invalidate1(CAULIFLOWER, OBJECT);

        BigInteger value6 = subject.cachedMethod2();
        assertNotSame(value4, value6);

        // If this fails, the interceptor may be recreating the same bean multiple times.
        assertEquals(1, DependentKeyGen.livingBeans);

        Object value7 = subject.cachedMethod3(/* Not used */ null, /* Not used */ null);
        Object value8 = subject.cachedMethod3(/* Not used */ null, /* Not used */ null);
        assertSame(value7, value8);

        subject.invalidate2(CAULIFLOWER, /* Not used */ null, "cachedMethod3");

        Object value9 = subject.cachedMethod3(/* Not used */ null, /* Not used */ null);
        assertNotSame(value8, value9);
    }

    @ApplicationScoped
    static class CachedService {

        private static final String CACHE_NAME = "test-cache";

        // This method is used to test a CDI injection into a cache key generator.
        public String getCauliflower() {
            return CAULIFLOWER;
        }

        @CacheResult(cacheName = CACHE_NAME, keyGenerator = DependentKeyGen.class)
        public BigInteger cachedMethod2() {
            return BigInteger.valueOf(new SecureRandom().nextInt());
        }

        // The cache key elements will vary depending on which annotation is evaluated during the interception.
        @CacheKeyParameterPositions(0)
        @CacheInvalidate(cacheName = CACHE_NAME, keyGenerator = RequestScopedKeyGen.class)
        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate1(String param0, Object param1) {
        }

        @CacheResult(cacheName = CACHE_NAME, keyGenerator = ApplicationScopedKeyGen.class)
        public Object cachedMethod3(/* Not used */ Object param0, /* Not used */ String param1) {
            return new Object();
        }

        @CacheInvalidate(cacheName = CACHE_NAME, keyGenerator = NotABeanKeyGen.class)
        public void invalidate2(/* Key element */ String param0, /* Not used */ Long param1, /* Key element */ String param2) {
        }
    }

    @ApplicationScoped
    public static class ApplicationScopedKeyGen implements CacheKeyGenerator {

        @PreDestroy
        void preDestroy() {
            System.out.println("A");
        }
        @Inject
        CachedService cachedService;

        @Override
        public Object generate(Method method, Object... methodParams) {
            return new CompositeCacheKey(method.getName(), cachedService.getCauliflower());
        }
    }

    @ApplicationScoped
    public static class RequestScopedKeyGen implements CacheKeyGenerator {

        @Override
        public Object generate(Method method, Object... methodParams) {
            return new CompositeCacheKey(ASPARAGUS, methodParams[1]);
        }
    }

    @Dependent
    public static class DependentKeyGen implements CacheKeyGenerator {

        // This counts how many beans of this key generator are currently alive.
        public static volatile int livingBeans;

        @PostConstruct
        void postConstruct() {
            livingBeans++;
        }

        @PreDestroy
        void preDestroy() {
            livingBeans--;
        }

        @Override
        public Object generate(Method method, Object... methodParams) {
            return CAULIFLOWER;
        }
    }

    public static class NotABeanKeyGen implements CacheKeyGenerator {

        @Override
        public Object generate(Method method, Object... methodParams) {
            return new CompositeCacheKey(methodParams[2], methodParams[0]);
        }
    }
}
