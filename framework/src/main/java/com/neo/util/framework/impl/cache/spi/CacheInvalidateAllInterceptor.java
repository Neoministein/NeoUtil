package com.neo.util.framework.impl.cache.spi;

import com.neo.util.common.impl.FutureUtils;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheManager;
import com.neo.util.framework.api.cache.spi.CacheInvalidateAll;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@CacheInvalidateAll(cacheName = "") // The `cacheName` attribute is @Nonbinding.
@Interceptor
@Priority(CacheInterceptor.BASE_PRIORITY)
public class CacheInvalidateAllInterceptor extends CacheInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInvalidateAllInterceptor.class);

    protected static final Function<Annotation, Set<CacheInvalidateAll>> CHECK_FOR_ANNOTATION_INSTANCE = methodAnnotation -> {
        if (methodAnnotation instanceof CacheInvalidateAll cacheInvalidateAll) {
            return Set.of(cacheInvalidateAll);
        }
        if (methodAnnotation instanceof CacheInvalidateAll.List list) {
            return Set.of(list.value());
        }
        return Set.of();
    };

    @Inject
    protected CacheInvalidateAllInterceptor(CacheManager cacheManager, CacheKeyGeneratorManager cacheKeyGeneratorManager) {
        super(cacheManager, cacheKeyGeneratorManager);
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Throwable {
        CacheInterceptionContext<CacheInvalidateAll> interceptionContext = getInterceptionContext(invocationContext,
                CHECK_FOR_ANNOTATION_INSTANCE);

        ReturnType returnType = determineReturnType(invocationContext.getMethod().getReturnType());
        if (returnType == ReturnType.NON_ASYNC) {
            return invalidateAllBlocking(invocationContext, interceptionContext);

        } else {
            return invalidateAllNonBlocking(invocationContext, interceptionContext);
        }
    }

    protected Object invalidateAllNonBlocking(InvocationContext invocationContext,
            CacheInterceptionContext<CacheInvalidateAll> interceptionContext) throws Throwable {
        CompletableFuture<?>[] completableFuture = interceptionContext.interceptorBindings().stream().map(this::invalidateAll)
                .toArray(CompletableFuture[]::new);

        CompletableFuture<Object> proceed = (CompletableFuture<Object>) invocationContext.proceed();

        return CompletableFuture.allOf(completableFuture).thenCompose(s -> proceed);
    }

    protected Object invalidateAllBlocking(InvocationContext invocationContext,
            CacheInterceptionContext<CacheInvalidateAll> interceptionContext) throws Throwable {
        LOGGER.trace("Invalidating all cache entries in a blocking way");
        for (CacheInvalidateAll binding : interceptionContext.interceptorBindings()) {
            FutureUtils.await(invalidateAll(binding));
        }
        return invocationContext.proceed();
    }

    protected CompletableFuture<Void> invalidateAll(CacheInvalidateAll binding) {
        Cache cache = cacheManager.getCache(binding.cacheName()).orElseThrow();
        LOGGER.debug("Invalidating all entries from cache [{}]", binding.cacheName());
        return cache.invalidateAllAsync();
    }
}
