package com.neo.util.framework.caffeine.impl;

import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.config.Config;

import java.util.Optional;

public record CaffeineCacheConfig(
        String cacheName,
        Optional<Long> expireAfterSeconds,
        Optional<Integer> initialCapacity,
        Optional<Integer> maxCapacity
        ) {
    public CaffeineCacheConfig(Config node) {
        this(
                node.key(),
                node.get(EXPIRE_AFTER_WRITE_CONFIG).asLong().asOptional(),
                node.get(INITIAL_CAPACITY_CONFIG).asInt().asOptional(),
                node.get(MAX_CACHE_SIZE_CONFIG).asInt().asOptional());
    }

    public CaffeineCacheConfig(Config node, CaffeineCacheConfig defaultConfig) {
        this(
                node.key(),
                node.get(EXPIRE_AFTER_WRITE_CONFIG).asLong().asOptional().or(defaultConfig::expireAfterSeconds),
                node.get(INITIAL_CAPACITY_CONFIG).asInt().asOptional().or(defaultConfig::initialCapacity),
                node.get(MAX_CACHE_SIZE_CONFIG).asInt().asOptional().or(defaultConfig::maxCapacity));
    }

    public CaffeineCacheConfig(String cacheName, CaffeineCacheConfig defaultConfig) {
        this(
                cacheName,
                defaultConfig.expireAfterSeconds(),
                defaultConfig.initialCapacity(),
                defaultConfig.maxCapacity()
        );
    }

    public static final String EXPIRE_AFTER_WRITE_CONFIG = "expireAfterSeconds";
    public static final String INITIAL_CAPACITY_CONFIG = "initialCapacity";
    public static final String MAX_CACHE_SIZE_CONFIG = "maxSize";

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
