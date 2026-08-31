package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.spi.CacheInvalidate;
import com.neo.util.framework.api.cache.spi.CacheInvalidateAll;
import com.neo.util.framework.api.cache.spi.CacheName;
import com.neo.util.framework.api.cache.spi.CacheResult;
import com.neo.util.framework.impl.ReflectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a {@link Set<String>} of all CacheNames
 */
@ApplicationScoped
public class CacheInstanceSearcher {

    protected final Set<String> cacheNames;

    @Inject
    public CacheInstanceSearcher(ReflectionService reflectionService) {
        Set<String> names = new HashSet<>();

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheName.class)) {
            names.add(element.getAnnotation(CacheName.class).value());
        }

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheResult.class)) {
            names.add(element.getAnnotation(CacheResult.class).cacheName());
        }

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheInvalidate.class)) {
            names.add(element.getAnnotation(CacheInvalidate.class).cacheName());
        }

        for (AnnotatedElement element: reflectionService.getAnnotatedElement(CacheInvalidateAll.class)) {
            names.add(element.getAnnotation(CacheInvalidateAll.class).cacheName());
        }

        //This is done to not create cache instances for the SPI references for the interceptor implementation
        names.remove("");

        this.cacheNames = Collections.unmodifiableSet(names);
    }

    public Set<String> getCacheNames() {
        return cacheNames;
    }
}
