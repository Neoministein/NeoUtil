package com.neo.util.framework.api.cache.spi;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines the cache key on for method arguments. It is used during an invocation of
 * a method annotated with {@link CacheResult} or {@link CacheInvalidate}.
 * <p>
 * {@link CacheKeyParameterPositions} should only be used when some method arguments are NOT part of the cache key.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface CacheKeyParameterPositions {

    @Nonbinding
    short[] value() default {};
}
