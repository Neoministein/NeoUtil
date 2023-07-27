package com.neo.util.framework.api.cache.spi;

import com.neo.util.framework.api.cache.CacheKeyGenerator;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotated with {@link CacheResult} is invoked when called from outside the current bean, a cache
 * key will be computed and use it to check in the cache whether the method has been already invoked.
 * <p>
 * The cache key is computed using the following logic:
 * <ul>
 * <li>If a {@link CacheKeyGenerator} is specified with this annotation, then it is used to generate the cache key.
 * The {@link CacheKeyParameterPositions} will then be ignored. </li>
 * <li>Otherwise, if the method has no arguments, then the name of the method will be used.</li>
 * <li>Otherwise, if the method has exactly one argument, then that argument is the cache key.</li>
 * <li>Otherwise, if the method has {@link CacheKeyParameterPositions} the defined parameter positions will then be used.</li>
 * <li>Otherwise, the cache key is an instance of {@link CompositeCacheKey} built from all the method arguments.</li>
 * </ul>
 * <p>
 * If a value is found in the cache, it is returned and the annotated method is never actually executed. If no value is found,
 * the annotated method is invoked and the returned value is stored in the cache using the computed key.
 * <p>
 * A method annotated with {@link CacheResult} is protected by a lock on cache miss mechanism. If several concurrent
 * invocations try to retrieve a cache value from the same missing key, the method will only be invoked once. The first
 * concurrent invocation will trigger the method invocation while the subsequent concurrent invocations will wait for the end
 * of the method invocation to get the cached result.
 * <p>
 * This annotation cannot be used on a method returning {@code void}. It can be combined with multiple other caching
 * annotations on a single method. Caching operations will always be executed in the same order: {@link CacheInvalidateAll}
 * first, then {@link CacheInvalidate} and finally {@link CacheResult}.
 */
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheResult {

    /**
     * The name of the cache.
     */
    @Nonbinding
    String cacheName();

    /**
     * The {@link CacheKeyGenerator} implementation to use when generation a cache key.
     */
    @Nonbinding
    Class<? extends CacheKeyGenerator> keyGenerator() default UndefinedCacheKeyGenerator.class;
}
