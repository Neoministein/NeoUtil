package com.neo.util.framework.impl.cache;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheManager;
import com.neo.util.framework.api.cache.spi.CacheName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import java.lang.annotation.Annotation;

@ApplicationScoped
public class CacheProducer {

    protected static final ExceptionDetails EX_NO_CACHE_FOUND = new ExceptionDetails(
            "cache/no-cache","The provided cache {0} name does not exist.", true);
    @Inject
    protected CacheManager cacheManager;

    @Produces
    @CacheName("") // The `value` attribute is @Nonbinding.
    public Cache produce(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier instanceof CacheName cacheName) {
                return cacheManager.getCache(cacheName.value()).orElseThrow(() ->
                        new ConfigurationException(EX_NO_CACHE_FOUND, cacheName.value())
                );
            }
        }
        //This should never occur otherwise the container provider has an issue
        return null;
    }
}
