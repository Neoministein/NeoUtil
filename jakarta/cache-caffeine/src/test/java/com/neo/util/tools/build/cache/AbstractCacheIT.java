package com.neo.util.tools.build.cache;

import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import com.neo.util.jakarta.cache.BasicCacheManagerImpl;
import com.neo.util.jakarta.cache.CacheInstanceSearcher;
import com.neo.util.jakarta.cache.spi.CacheKeyGeneratorManager;
import com.neo.util.jakarta.cache.spi.annotation.CacheName;
import com.neo.util.jakarta.caffeine.CaffeineCacheBuilder;
import com.neo.util.jakarta.config.JakartaConfigService;
import com.neo.util.jakarta.reflexion.JakartaReflectionProviderWrapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractCacheIT {
    @CacheName(AbstractCacheIT.BASIC_TEST_CACHE_1) //Required for cache to be instantiated
    public static final String BASIC_TEST_CACHE_1 = "testCache1";

    @WeldSetup
    protected WeldInitiator weld = WeldInitiator.from(
            basicCDIClasses().toArray(new Class[0])
    ).activate(ApplicationScoped.class, RequestScoped.class).build();

    protected List<Class<?>> basicCDIClasses() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(BasicCacheManagerImpl.class);
        classes.add(CacheKeyGeneratorManager.class);
        classes.add(CaffeineCacheBuilder.class);
        classes.add(JakartaConfigService.class);
        classes.add(IndexReflectionProvider.class);
        classes.add(JakartaReflectionProviderWrapper.class);
        classes.add(CacheInstanceSearcher.class);
        return classes;
    }

    protected Map<String, Object> getConfig() {
        return new HashMap<>();
    }

    protected CacheName getCacheNameInstance(String value) {
        return new CacheName() {

            @Override
            public String value() {
                return value;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return CacheName.class;
            }
        };
    }
}
