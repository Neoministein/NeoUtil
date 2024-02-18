package com.neo.util.framework.impl.cache.spi;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheManager;
import com.neo.util.framework.api.cache.spi.CachePut;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@CachePut(valueParameterPosition = 0 ,cacheName = "") // The `value` and `cacheName` attribute is @Nonbinding.
@Interceptor
@Priority(CacheInterceptor.BASE_PRIORITY + 2)
public class CachePutInterceptor extends CacheInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachePutInterceptor.class);

    @Inject
    public CachePutInterceptor(CacheManager cacheManager, CacheKeyGeneratorManager cacheKeyGeneratorManager) {
        super(cacheManager, cacheKeyGeneratorManager);
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Throwable {
        CacheInterceptionContext<CachePut> interceptionContext = getInterceptionContext(invocationContext,
                method -> method instanceof CachePut cacheResult ? Set.of(cacheResult) : Set.of());

        CachePut binding = interceptionContext.interceptorBindings().get(0);
        Cache cache = cacheManager.getCache(binding.cacheName()).orElseThrow();
        Object key = getCacheKey(binding.keyGenerator(), interceptionContext.cacheKeyParameterPositions(),
                invocationContext.getMethod(), invocationContext.getParameters());
        LOGGER.debug("Setting cache entry with key [{}] from cache [{}]", key, binding.cacheName());

        cache.put(key, invocationContext.getParameters()[binding.valueParameterPosition()]);
        return invocationContext.proceed();
    }
}