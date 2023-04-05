package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.api.cache.CacheBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ApplicationScoped
public class DummyCacheBuilder implements CacheBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyCacheBuilder.class);

    @Override
    public Map<String, Cache> build() {
        LOGGER.warn("No cache impl specified");
        return Map.of();
    }
}
