package com.neo.util.framework.api.cache.spi;

import com.neo.util.framework.api.cache.CacheKeyGenerator;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotated with {@link CachePut} is invoked when called from outside the current bean, a cache
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
 * This annotation can be combined with multiple other caching annotations on a single method. Caching operations will always
 * be executed in the same order: {@link CacheInvalidateAll} first, then {@link CacheInvalidate},
 * then {@link CachePut} and finally {@link CacheResult}.
 */
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CachePut {

    /**
     * The index of the parameters which represents the object ot be cached starting at 0
     */
    @Nonbinding
    int valueParameterPosition();

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
