package com.neo.util.framework.caffeine.impl;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.impl.cache.AbstractCacheBuilder;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Alternative
@Priority(PriorityConstants.APPLICATION)
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
            LOGGER.debug("Registered CaffeineCache, {}", config);
            caffeineCacheMap.put(config.cacheName(), new CaffeineCache(config));
        }

        LOGGER.info("Registered [{}] CaffeineCaches [{}]",caffeineCacheMap.size() , caffeineCacheMap.keySet());
        return caffeineCacheMap;
    }


    public List<CaffeineCacheConfig> getConfigs() {
        Set<String> reflectionConfig = getCacheNames();
        Config config = configService.get(CONFIG_PREFIX);
        CaffeineCacheConfig defaultConfig = new CaffeineCacheConfig(config.get(DEFAULT_CONFIG));
        LOGGER.trace("Default CaffeineCacheConfig loaded Config: {}", defaultConfig);

        ConfigValue<List<CaffeineCacheConfig>> caffeineCacheConfigs = config.get(INSTANCES_CONFIG).asList(node -> {
            reflectionConfig.remove(node.key());
            LOGGER.trace("Creating CaffeineCacheConfig from Config: {}", node.key());
            return new CaffeineCacheConfig(node, defaultConfig);
        });

        LOGGER.trace("Creating rest of the CaffeineCacheConfig from reflections {}", reflectionConfig);
        //Do not use async stream you will get an index out of bound exception
        return reflectionConfig.stream().map(cacheName -> new CaffeineCacheConfig(cacheName, defaultConfig))
                .collect(Collectors.toCollection(() -> caffeineCacheConfigs.orElse(new ArrayList<>())));
    }
}
