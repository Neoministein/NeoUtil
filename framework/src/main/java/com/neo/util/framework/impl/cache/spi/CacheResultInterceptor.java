package com.neo.util.framework.impl.cache.spi;

import com.neo.util.common.impl.FutureUtils;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.spi.CacheResult;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CacheResult(cacheName = "") // The `cacheName` attribute is @Nonbinding.
@Interceptor
@Priority(CacheInterceptor.BASE_PRIORITY + 2)
public class CacheResultInterceptor extends CacheInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheResultInterceptor.class);

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Throwable {
        CacheInterceptionContext<CacheResult> interceptionContext = getInterceptionContext(invocationContext,
                method -> method instanceof CacheResult cacheResult ? Set.of(cacheResult) : Set.of());

        CacheResult binding = interceptionContext.interceptorBindings().get(0);
        Cache cache = cacheManager.getCache(binding.cacheName()).orElseThrow();
        Object key = getCacheKey(cache, binding.keyGenerator(), interceptionContext.cacheKeyParameterPositions(),
                invocationContext.getMethod(), invocationContext.getParameters());
        LOGGER.debug("Loading cache entry with key [{}] from cache [{}]", key, binding.cacheName());

        try {
            ReturnType returnType = determineReturnType(invocationContext.getMethod().getReturnType());

            CompletableFuture<Object> cacheValue = cache.getAsync(key, ignored -> {
                try {
                    return invocationContext.proceed();
                } catch (CacheException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new CacheException(ex);
                }
            });

            if (returnType == ReturnType.COMPLETION_STAGE) {
                return cacheValue;
            }

            if (returnType == ReturnType.NON_ASYNC) {
                return FutureUtils.await(cacheValue);
            }

            throw new UnsupportedOperationException(returnType.toString());
        } catch (CacheException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            } else {
                throw ex;
            }
        }
    }

}
