package com.neo.util.framework.rest.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.rest.cache.api.CachedObject;
import com.neo.util.framework.rest.cache.api.ServerCacheService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CaffeineServerCacheService implements ServerCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineServerCacheService.class);

    //Config
    protected static final String CONFIG_PREFIX = "cache";
    protected static final String EXPIRE_AFTER_WRITE_CONFIG = CONFIG_PREFIX + ".global.expireAfterWrite";
    protected static final String EXPIRE_AFTER_ACCESS_CONFIG = CONFIG_PREFIX + ".global.expireAfterAccess";
    protected static final String INITIAL_CAPACITY_CONFIG = CONFIG_PREFIX + ".initialCapacity";
    protected static final String MAX_CACHE_SIZE_CONFIG = CONFIG_PREFIX + ".maxSize";

    @Inject
    protected ConfigService configService;

    protected Cache<String, CachedObject> cache;

    @PostConstruct
    public void init() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        ConfigValue<Long> expireAfterAccess = configService.get(EXPIRE_AFTER_WRITE_CONFIG).asLong();
        if (expireAfterAccess.isPresent()) {
            LOGGER.info("Caffeine.Builder Expire After Access: {}", expireAfterAccess.get());
            builder.expireAfterAccess(expireAfterAccess.get(), TimeUnit.SECONDS);
        }

        ConfigValue<Long> expireAfterWrite = configService.get(EXPIRE_AFTER_ACCESS_CONFIG).asLong();
        if (expireAfterWrite.isPresent()) {
            LOGGER.info("Caffeine.Builder Expire After Write: {}", expireAfterWrite.get());
            builder.expireAfterWrite(expireAfterWrite.get(), TimeUnit.SECONDS);
        }

        int initialCapacity = configService.get(INITIAL_CAPACITY_CONFIG).asInt().orElse(16);
        LOGGER.info("Caffeine.Builder Initial Capacity: {}", initialCapacity);
        builder.initialCapacity(initialCapacity);

        long maxSize = configService.get(MAX_CACHE_SIZE_CONFIG).asLong().orElse(1000L);
        LOGGER.info("Caffeine.Builder maximum Size: {}", maxSize);
        builder.maximumSize(maxSize);

        cache = builder.build();
    }

    @Override
    public Optional<CachedObject> get(String key) {
        LOGGER.trace("Hitting caffeine cache with key [{}]", key);
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public Optional<CachedObject> getMaxAge(String key, Duration maxAge) {
        if (maxAge.isNegative()) {
            LOGGER.warn("Received a negative value for maxAge");
            return Optional.empty();
        }
        return getMaxAge(key, TimeUnit.MILLISECONDS, maxAge.toMillis());
    }

    @Override
    public Optional<CachedObject> getMaxAge(String key, TimeUnit timeUnit, long maxAge) {
        Optional<CachedObject> optionalCachedObject = get(key);
        if (optionalCachedObject.isEmpty()) {
            return optionalCachedObject;
        }

        if (optionalCachedObject.get().cacheTime() + timeUnit.toMillis(maxAge) < System.currentTimeMillis()) {
            LOGGER.debug("MaxAge has been exceeded for cache entry [{}] ", key);
            invalidate(key);
            return Optional.empty();
        }

        return optionalCachedObject;
    }

    @Override
    public void put(String key, CachedObject value) {
        LOGGER.debug("Caching value for key [{}]", key);
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<String, CachedObject> map) {
        LOGGER.debug("Bulk caching values for keys [{}]", map.keySet());
        cache.putAll(map);
    }

    @Override
    public void invalidate(String key) {
        LOGGER.debug("Invalidating cache for key [{}]", key);
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll(Iterable<String> keys) {
        LOGGER.debug("Bulk invalidating cache for keys [{}]", keys);
        cache.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        LOGGER.info("Invalidating entire cache");
        cache.invalidateAll();
    }

    public void onStartUp(@Observes ApplicationPostReadyEvent applicationPostReadyEvent) {
        LOGGER.debug("ApplicationPostReadyEvent received");
    }
}
