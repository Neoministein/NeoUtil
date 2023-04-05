package com.neo.util.framework.api.cache.spi;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

/**
 * A method annotated with {@link CacheInvalidateAll} is invoked when called from outside the current bean and
 * will remove all entries from the cache.
 * <p>
 * This annotation can be combined with multiple other caching annotations on a single method. Caching operations will always
 * be executed in the same order: {@link CacheInvalidateAll} first, then {@link CacheInvalidate} and finally
 * {@link CacheResult}.
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CacheInvalidateAll.List.class)
public @interface CacheInvalidateAll {

    /**
     * The name of the cache.
     */
    @Nonbinding
    String cacheName();

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        CacheInvalidateAll[] value();
    }
}
