package com.neo.util.framework.impl.cache;

import com.neo.util.common.impl.annotation.JandexUtils;
import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.cache.spi.CacheInvalidate;
import com.neo.util.framework.api.cache.spi.CacheInvalidateAll;
import com.neo.util.framework.api.cache.spi.CacheName;
import com.neo.util.framework.api.cache.spi.CacheResult;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Basic Abstract impl of {@link CacheBuilder} which provides methods to search for cache annotation instances
 */
public abstract class AbstractCacheBuilder implements CacheBuilder {

    protected static final String CACHE_NAME = "cacheName";

    public Set<String> getCacheNames() {
        Set<String> cacheNames;
        Optional<Index> index = JandexUtils.getIndex();
        if (index.isPresent()) {
            cacheNames = getCacheNamesByAnnotation(index.get());
        } else {
            cacheNames = getCacheNamesByAnnotation();
        }
        return cacheNames;
    }

    protected Set<String> getCacheNamesByAnnotation(Index index) {
        Set<String> cacheNames = new HashSet<>();

        for (Class<? extends Annotation> annotation: List.of(CacheResult.class, CacheInvalidate.class, CacheInvalidateAll.class)) {
            for (AnnotationInstance annotationInstance: index.getAnnotations(DotName.createSimple(annotation.getName()))) {
                cacheNames.add(annotationInstance.value(CACHE_NAME).asString());
            }
        }

        for (AnnotationInstance annotationInstance: index.getAnnotations(DotName.createSimple(CacheName.class.getName()))) {
            cacheNames.add(annotationInstance.value("value").asString());
        }

        return cacheNames;
    }

    protected Set<String> getCacheNamesByAnnotation() {
        Set<String> cacheNames = new HashSet<>();

        for (CacheName annotation: ReflectionUtils.getAnnotationInstances(CacheName.class)) {
            cacheNames.add(annotation.value());
        }

        for (CacheResult annotation: ReflectionUtils.getAnnotationInstances(CacheResult.class)) {
            cacheNames.add(annotation.cacheName());
        }
        for (CacheInvalidate annotation: ReflectionUtils.getAnnotationInstances(CacheInvalidate.class)) {
            cacheNames.add(annotation.cacheName());
        }
        for (CacheInvalidateAll annotation: ReflectionUtils.getAnnotationInstances(CacheInvalidateAll.class)) {
            cacheNames.add(annotation.cacheName());
        }
        return cacheNames;
    }
}
