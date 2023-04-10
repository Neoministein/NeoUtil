package com.neo.util.framework.impl.cache.spi;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheKeyGenerator;
import com.neo.util.framework.api.cache.CacheManager;
import com.neo.util.framework.api.cache.spi.CacheKeyParameterPositions;
import com.neo.util.framework.api.cache.spi.CompositeCacheKey;
import com.neo.util.framework.api.cache.spi.UndefinedCacheKeyGenerator;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor.Priority;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public abstract class CacheInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInterceptor.class);

    public static final int BASE_PRIORITY = Priority.PLATFORM_BEFORE;

    @Inject
    protected CacheKeyGeneratorManager cacheKeyGeneratorManager;

    @Inject
    protected CacheManager cacheManager;

    protected <T> CacheInterceptionContext<T> getInterceptionContext(InvocationContext invocationContext,
            Function<Annotation, Set<T>> interceptorBindingFunc) {
        LOGGER.trace("Retrieving interceptor bindings using reflection");
        List<T> interceptorBindings = new ArrayList<>();
        List<Short> cacheKeyParameterPositions = new ArrayList<>();
        for (Annotation methodAnnotation : invocationContext.getMethod().getAnnotations()) {
            if (methodAnnotation instanceof CacheKeyParameterPositions annotation) {
                for (short position : annotation.value()) {
                    cacheKeyParameterPositions.add(position);
                }
            } else {
                interceptorBindings.addAll(interceptorBindingFunc.apply(methodAnnotation));
            }
        }

        return new CacheInterceptionContext<>(interceptorBindings, cacheKeyParameterPositions);
    }

    protected Object getCacheKey(Cache cache, Class<? extends CacheKeyGenerator> keyGeneratorClass,
                                 List<Short> cacheKeyParameterPositions, Method method, Object[] methodParameterValues) {
        if (keyGeneratorClass != UndefinedCacheKeyGenerator.class) {
            return cacheKeyGeneratorManager.getCacheKeyGenerator(keyGeneratorClass)
                    .generate(method, methodParameterValues);
        } else if (methodParameterValues == null || methodParameterValues.length == 0) {
            // If the intercepted method doesn't have any parameter, then the default cache key will be used.
            return cache.getName();
        } else if (cacheKeyParameterPositions.size() == 1) {
            // If @CacheKeyParameterPositions has only one parameter in the array, then this parameter will be used as the cache key.
            return methodParameterValues[cacheKeyParameterPositions.get(0)];
        } else if (cacheKeyParameterPositions.size() >= 2) {
            // If @CacheKeyParameterPositions has two or more parameters in the array, then a composite cache key built
            // from all these parameters will be used.
            List<Object> keyElements = new ArrayList<>();
            for (short position : cacheKeyParameterPositions) {
                keyElements.add(methodParameterValues[position]);
            }
            return new CompositeCacheKey(keyElements.toArray(new Object[0]));
        } else if (methodParameterValues.length == 1) {
            // If the intercepted method has exactly one parameter, then this parameter will be used as the cache key.
            return methodParameterValues[0];
        } else {
            // If the intercepted method has two or more parameters, then a composite cache key built from all these parameters
            // will be used.
            return new CompositeCacheKey(methodParameterValues);
        }
    }

    protected static ReturnType determineReturnType(Class<?> returnType) {
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            return ReturnType.COMPLETION_STAGE;
        }
        return ReturnType.NON_ASYNC;
    }

    protected enum ReturnType {
        NON_ASYNC,
        COMPLETION_STAGE
    }
}
