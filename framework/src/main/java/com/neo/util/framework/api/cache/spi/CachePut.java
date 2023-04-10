package com.neo.util.framework.api.cache.spi;

import com.neo.util.framework.api.cache.CacheKeyGenerator;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
