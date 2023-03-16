package com.neo.util.framework.rest.cache.api;

import java.util.concurrent.TimeUnit;

public record CachedObject(Object entity, long cacheTime) {

    public CachedObject(Object entity) {
        this(entity, System.currentTimeMillis());
    }

    public long getTimeSinceCached() {
        return getTimeSinceCached(TimeUnit.SECONDS);
    }

    public long getTimeSinceCached(TimeUnit timeUnit) {
        return timeUnit.convert(System.currentTimeMillis() - cacheTime, TimeUnit.MILLISECONDS);
    }
}
