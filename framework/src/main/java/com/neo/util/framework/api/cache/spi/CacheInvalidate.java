package com.neo.util.framework.api.cache.spi;

import com.neo.util.framework.api.cache.CacheKeyGenerator;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

/**
 * A method annotated with {@link CacheInvalidate} is invoked when called from outside the current bean, a cache
 * key will be computed and use it to try to remove an existing entry from the cache.
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
 * If the key does not identify any cache entry, nothing will happen.
 * <p>
 * This annotation can be combined with multiple other caching annotations on a single method. Caching operations will always
 * be executed in the same order: {@link CacheInvalidateAll} first, then {@link CacheInvalidate},
 * then {@link CachePut} and finally {@link CacheResult}.
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CacheInvalidate.List.class)
public @interface CacheInvalidate {

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


    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        CacheInvalidate[] value();
    }
}
