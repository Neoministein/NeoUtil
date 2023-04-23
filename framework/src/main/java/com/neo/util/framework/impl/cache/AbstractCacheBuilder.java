package com.neo.util.framework.impl.cache;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.cache.spi.CacheInvalidate;
import com.neo.util.framework.api.cache.spi.CacheInvalidateAll;
import com.neo.util.framework.api.cache.spi.CacheName;
import com.neo.util.framework.api.cache.spi.CacheResult;
import com.neo.util.framework.impl.JandexService;
import jakarta.inject.Inject;
import org.jboss.jandex.AnnotationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic Abstract impl of {@link CacheBuilder} which provides methods to search for cache annotation instances
 */
public abstract class AbstractCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCacheBuilder.class);

    protected static final String CACHE_NAME = "cacheName";

    @Inject
    protected JandexService jandexService;

    public Set<String> getCacheNames() {
        Set<String> cacheNames;
        if (jandexService.getIndex().isPresent()) {
            cacheNames = getCacheNamesByJandex();
        } else {
            LOGGER.warn("Unable to load Jandex Index. Falling back to reflections, this can drastically increase load time.");
            cacheNames = getCacheNamesByReflection();
        }
        return cacheNames;
    }

    protected Set<String> getCacheNamesByJandex() {
        Set<String> cacheNames = new HashSet<>();

        for (Class<? extends Annotation> annotation: List.of(CacheResult.class, CacheInvalidate.class, CacheInvalidateAll.class)) {
            for (AnnotationInstance annotationInstance: jandexService.getAnnotationInstance(annotation)) {
                cacheNames.add(annotationInstance.value(CACHE_NAME).asString());
            }
        }

        for (AnnotationInstance annotationInstance: jandexService.getAnnotationInstance(CacheName.class)) {
            cacheNames.add(annotationInstance.value("value").asString());
        }

        return cacheNames;
    }

    protected Set<String> getCacheNamesByReflection() {
        Set<String> cacheNames = new HashSet<>();

        for (AnnotatedElement element: ReflectionUtils.getAnnotatedElement(CacheName.class)) {
            cacheNames.add(element.getAnnotation(CacheName.class).value());
        }

        for (AnnotatedElement element: ReflectionUtils.getAnnotatedElement(CacheResult.class)) {
            cacheNames.add(element.getAnnotation(CacheResult.class).cacheName());
        }
        for (AnnotatedElement element: ReflectionUtils.getAnnotatedElement(CacheInvalidate.class)) {
            cacheNames.add(element.getAnnotation(CacheInvalidate.class).cacheName());
        }
        for (AnnotatedElement element: ReflectionUtils.getAnnotatedElement(CacheInvalidateAll.class)) {
            cacheNames.add(element.getAnnotation(CacheInvalidateAll.class).cacheName());
        }
        return cacheNames;
    }
}
