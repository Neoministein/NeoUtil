package com.neo.util.framework.impl.cache;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.framework.api.cache.Cache;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(WeldJunit5Extension.class)
class CacheNameIT extends AbstractCacheIT {

    @BeforeEach
    public void before() {
        super.setupConfig();
    }

    @Test
    void injectSuccessfully() {
        //The cache injection should be successful
        Cache cache = weld.select(Cache.class, getCacheNameInstance(BASIC_TEST_CACHE_1)).get();

        Assertions.assertNotNull(cache);
    }

    @Test
    void injectFailureNoAnnotation() {
        //The cache injection should fail due to CacheName annotation missing
        Assertions.assertThrows(UnsatisfiedResolutionException.class,() -> weld.select(Cache.class).get());
    }

    @Test
    void injectFailureNoValidCache() {
        //The cache injection should fail due to CacheName not existing
        Assertions.assertThrows(ConfigurationException.class,() -> weld.select(Cache.class, getCacheNameInstance("Non-Existent-Cache")).get());
    }

    @Override
    protected List<Class<?>> basicCDIClasses() {
        List<Class<?>> classes = super.basicCDIClasses();
        classes.add(CacheProducer.class);
        return classes;
    }
}
