package com.neo.util.framework.caffeine.impl;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.impl.cache.CacheInstanceSearcher;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Alternative
@Priority(PriorityConstants.APPLICATION)
@ApplicationScoped
public class CaffeineCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineCacheBuilder.class);

    protected final ConfigService configService;
    protected final Set<String> cacheInstances;

    @Inject
    public CaffeineCacheBuilder(ConfigService configService, CacheInstanceSearcher cacheInstanceSearcher) {
        this.configService = configService;
        this.cacheInstances = cacheInstanceSearcher.getCacheNames();
    }

    @Override
    public Map<String, Cache> build() {
        LOGGER.info("Building all caffeine cache instances");
        List<CaffeineCacheConfig> configs = getConfigs();

        Map<String, Cache> caffeineCacheMap = new HashMap<>(configs.size() + 1, 1.0F);
        for (CaffeineCacheConfig config: configs) {
            LOGGER.debug("Registered CaffeineCache, {}", config);
            caffeineCacheMap.put(config.cacheName(), new CaffeineCache(config));
        }

        LOGGER.info("Registered [{}] CaffeineCaches [{}]",caffeineCacheMap.size() , caffeineCacheMap.keySet());
        return caffeineCacheMap;
    }


    public List<CaffeineCacheConfig> getConfigs() {
        CaffeineCacheConfig defaultConfig = createDefault();
        LOGGER.trace("Default CaffeineCacheConfig loaded Config: {}", defaultConfig);
        List<CaffeineCacheConfig> configs = new ArrayList<>();
        for (String configName: cacheInstances) {
            LOGGER.trace("Creating CaffeineCacheConfig from Config: {}", configName);
            configs.add(createConfig(configName, defaultConfig));
        }
        return configs;
    }

    public CaffeineCacheConfig createConfig(String cacheName, CaffeineCacheConfig defaultConfig) {
        return new CaffeineCacheConfig(
                cacheName,
                configService.getAsLong("caffeine", cacheName ,"expireAfterSeconds").asOptional().or(defaultConfig::expireAfterSeconds),
                configService.getAsInt("caffeine", cacheName ,"initialCapacity").asOptional().or(defaultConfig::initialCapacity),
                configService.getAsInt("caffeine", cacheName ,"maxSize").asOptional().or(defaultConfig::maxCapacity));
    }

    private CaffeineCacheConfig createDefault() {
        return new CaffeineCacheConfig(
                "default",
                configService.getAsLong("caffeine.default.expireAfterSeconds").asOptional(),
                configService.getAsInt("caffeine.default.initialCapacity").asOptional(),
                configService.getAsInt("caffeine.default.maxSize").asOptional());
    }
}
