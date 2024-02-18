package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.impl.ReflectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class HashMapCacheBuilder extends AbstractCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapCacheBuilder.class);

    @Inject
    public HashMapCacheBuilder(ReflectionService reflectionService) {
        super(reflectionService);
    }

    @Override
    public Map<String, Cache> build() {
        LOGGER.warn("No Cache impl specified, using hashmaps");
        Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
        for (String cacheName: getCacheNames()) {
            cacheMap.put(cacheName, new HashMapCache(cacheName));
        }
        return cacheMap;
    }
}
