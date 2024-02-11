package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DummyCacheBuilder extends AbstractCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyCacheBuilder.class);

    @Override
    public Map<String, Cache> build() {
        LOGGER.warn("No Cache impl specified, using hashmaps");
        Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
        for (String cacheName: getCacheNames()) {
            cacheMap.put(cacheName, new BasicCache(cacheName));
        }
        return cacheMap;
    }
}
