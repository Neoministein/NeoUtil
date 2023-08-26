package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.cache.CacheManager;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class BasicCacheManagerImpl implements CacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCacheManagerImpl.class);

    protected Set<String> keys;
    protected Map<String, Cache> cacheMap = new HashMap<>();

    @Inject
    protected CacheBuilder cacheBuilder;

    @PostConstruct
    protected void initializeCaches() {
        cacheMap = cacheBuilder.build();
        keys = cacheMap.keySet();
    }

    @Override
    public Set<String> getCacheNames() {
        return keys;
    }

    @Override
    public Optional<Cache> getCache(String name) {
        return Optional.ofNullable(cacheMap.get(name));
    }

    @Override
    public void reload() {
        initializeCaches();
    }

    protected void onStartUp(@Observes ApplicationPreReadyEvent preReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent processed");
    }
}
