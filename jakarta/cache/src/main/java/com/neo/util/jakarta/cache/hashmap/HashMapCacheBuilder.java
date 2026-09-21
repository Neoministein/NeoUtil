package com.neo.util.jakarta.cache.hashmap;

import com.neo.util.api.cache.Cache;
import com.neo.util.api.cache.CacheBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class HashMapCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapCacheBuilder.class);

    @Override
    public Map<String, Cache> build(Set<String> names) {
        LOGGER.warn("No Cache impl specified, using hashmaps");
        Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
        for (String cacheName: names) {
            cacheMap.put(cacheName, new HashMapCache(cacheName));
        }
        return cacheMap;
    }
}
