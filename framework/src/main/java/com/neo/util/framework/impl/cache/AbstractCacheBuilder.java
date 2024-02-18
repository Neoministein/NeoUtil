package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.cache.spi.CacheInvalidate;
import com.neo.util.framework.api.cache.spi.CacheInvalidateAll;
import com.neo.util.framework.api.cache.spi.CacheName;
import com.neo.util.framework.api.cache.spi.CacheResult;
import com.neo.util.framework.impl.ReflectionService;

import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic Abstract impl of {@link CacheBuilder} which provides methods to search for cache annotation instances
 */
public abstract class AbstractCacheBuilder implements CacheBuilder {


    protected final ReflectionService reflectionService;

    protected AbstractCacheBuilder(ReflectionService reflectionService) {
        this.reflectionService = reflectionService;
    }

    public Set<String> getCacheNames() {
        Set<String> cacheNames = new HashSet<>();

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheName.class)) {
            cacheNames.add(element.getAnnotation(CacheName.class).value());
        }

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheResult.class)) {
            cacheNames.add(element.getAnnotation(CacheResult.class).cacheName());
        }

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheInvalidate.class)) {
            cacheNames.add(element.getAnnotation(CacheInvalidate.class).cacheName());
        }

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheInvalidateAll.class)) {
            cacheNames.add(element.getAnnotation(CacheInvalidateAll.class).cacheName());
        }

        //This is done to not create cache instances for the SPI references for the interceptor implementation
        cacheNames.remove("");

        return cacheNames;
    }
}
