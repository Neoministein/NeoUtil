package com.neo.util.framework.impl.cache.spi;

import com.neo.util.common.impl.FutureUtils;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.spi.CacheInvalidate;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@CacheInvalidate(cacheName = "") // The `cacheName` attribute is @Nonbinding.
@Interceptor
@Priority(CacheInterceptor.BASE_PRIORITY + 1)
public class CacheInvalidateInterceptor extends CacheInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInvalidateInterceptor.class);

    protected static final Function<Annotation, Set<CacheInvalidate>> CHECK_FOR_ANNOTATION_INSTANCE = methodAnnotation -> {
        if (methodAnnotation instanceof CacheInvalidate cacheInvalidate) {
            return Set.of(cacheInvalidate);
        }
        if (methodAnnotation instanceof CacheInvalidate.List list) {
            return Set.of(list.value());
        }
        return Set.of();
    };

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Throwable {
        CacheInterceptionContext<CacheInvalidate> interceptionContext = getInterceptionContext(invocationContext,
                CHECK_FOR_ANNOTATION_INSTANCE);
        ReturnType returnType = determineReturnType(invocationContext.getMethod().getReturnType());
        if (returnType == ReturnType.NON_ASYNC) {
            return invalidateBlocking(invocationContext, interceptionContext);
        } else {
            return invalidateNonBlocking(invocationContext, interceptionContext);
        }
    }

    protected Object invalidateNonBlocking(InvocationContext invocationContext,
            CacheInterceptionContext<CacheInvalidate> interceptionContext) throws Throwable {
        LOGGER.trace("Invalidating cache entries in a non-blocking way");
        CompletableFuture<?>[] completableFuture = interceptionContext.interceptorBindings().stream().map(cacheInvalidate ->
                                invalidate(cacheInvalidate, interceptionContext.cacheKeyParameterPositions(), invocationContext))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<Object> proceed = (CompletableFuture<Object>) invocationContext.proceed();

        return CompletableFuture.allOf(completableFuture).thenCompose(s -> proceed);
    }

    protected Object invalidateBlocking(InvocationContext invocationContext,
            CacheInterceptionContext<CacheInvalidate> interceptionContext) throws Throwable {
        LOGGER.trace("Invalidating cache entries in a blocking way");
        for (CacheInvalidate binding : interceptionContext.interceptorBindings()) {
            FutureUtils.await(invalidate(binding, interceptionContext.cacheKeyParameterPositions(), invocationContext),
                    ex -> { throw new CacheException(ex);});
        }
        return invocationContext.proceed();
    }

    protected CompletableFuture<Void> invalidate(CacheInvalidate binding, List<Short> cacheKeyParameterPositions,
                                               InvocationContext invocationContext) {
        Cache cache = cacheManager.getCache(binding.cacheName()).orElseThrow();
        Object key = getCacheKey(cache, binding.keyGenerator(), cacheKeyParameterPositions, invocationContext.getMethod(),
                invocationContext.getParameters());
        LOGGER.debug("Invalidating entry with key [{}] from cache [{}]", key, binding.cacheName());
        return cache.invalidateAsync(key);
    }
}
