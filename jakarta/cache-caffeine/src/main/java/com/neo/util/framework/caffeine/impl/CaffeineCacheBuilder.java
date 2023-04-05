package com.neo.util.framework.caffeine.impl;

import com.neo.util.framework.impl.cache.AbstractCacheBuilder;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CaffeineCacheBuilder extends AbstractCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineCacheBuilder.class);

    protected static final String CONFIG_PREFIX = "caffeine";
    protected static final String DEFAULT_CONFIG = "default";
    protected static final String INSTANCES_CONFIG = "instances";

    @Inject
    protected ConfigService configService;

    @Override
    public Map<String, Cache> build() {
        LOGGER.info("Building all caffeine cache instances");
        List<CaffeineCacheConfig> configs = getConfigs();

        Map<String, Cache> caffeineCacheMap = new HashMap<>(configs.size() + 1, 1.0F);
        for (CaffeineCacheConfig config: configs) {
            LOGGER.debug("Building caffeine instance with config {}", config);
            caffeineCacheMap.put(config.cacheName(), new CaffeineCache(config));
        }
        LOGGER.info("Finished building caffeine cache instances");
        return caffeineCacheMap;
    }


    public List<CaffeineCacheConfig> getConfigs() {
        Set<String> reflectionConfig = getCacheNames();
        Config config = configService.get(CONFIG_PREFIX);
        CaffeineCacheConfig defaultConfig = new CaffeineCacheConfig(config.get(DEFAULT_CONFIG));
        LOGGER.trace("Default CaffeineCacheConfig loaded {}", defaultConfig);

        ConfigValue<List<CaffeineCacheConfig>> caffeineCacheConfigs = config.get(INSTANCES_CONFIG).asList(node -> {
            reflectionConfig.remove(node.key());
            LOGGER.trace("Creating CaffeineCacheConfig from config {}", node.key());
            return new CaffeineCacheConfig(node, defaultConfig);
        });

        LOGGER.trace("Creating rest of the CaffeineCacheConfig from reflections {}", reflectionConfig);
        //Do not use async stream you will get an index out of bound exception
        return reflectionConfig.stream().map(cacheName -> new CaffeineCacheConfig(cacheName, defaultConfig))
                .collect(Collectors.toCollection(() -> caffeineCacheConfigs.orElse(new ArrayList<>())));
    }
}
