package com.neo.util.framework.caffeine.impl;

import java.util.Optional;

public record CaffeineCacheConfig(
        String cacheName,
        Optional<Long> expireAfterSeconds,
        Optional<Integer> initialCapacity,
        Optional<Integer> maxCapacity
        ) {

    @Override
    public String toString() {
        return "CacheName: [" + cacheName
                + "], ExpireAfterSeconds: [" + expireAfterSeconds.orElse(-1L)
                + "], InitialCapacity: [" + initialCapacity.orElse(-1)
                + "], MaxCapacity: [" + maxCapacity.orElse(-1) + "]";
    }
}
