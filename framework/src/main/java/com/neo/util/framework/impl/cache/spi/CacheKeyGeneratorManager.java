package com.neo.util.framework.impl.cache.spi;

import com.neo.util.framework.api.cache.CacheKeyGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class can be used to retrieve a {@link CacheKeyGenerator} object or instantiate it if it doesn't exist
 */
@ApplicationScoped
public class CacheKeyGeneratorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheKeyGeneratorManager.class);

    protected Map<Class<? extends CacheKeyGenerator>, CacheKeyGenerator> generators = new ConcurrentHashMap<>();

    @Inject
    protected Instance<CacheKeyGenerator> keyGenerators;

    public CacheKeyGenerator getCacheKeyGenerator(Class<? extends CacheKeyGenerator> keyGeneratorClass) {
        return generators.computeIfAbsent(keyGeneratorClass, this::buildCacheKeyGenerator);
    }

    protected CacheKeyGenerator buildCacheKeyGenerator(Class<? extends CacheKeyGenerator> keyGeneratorClass) {
        Instance<? extends CacheKeyGenerator> keyGenInstance = keyGenerators.select(keyGeneratorClass);
        if (keyGenInstance.isResolvable()) {
            LOGGER.trace("Generating CacheKeyGenerator bean from CDI of class [{}]", keyGeneratorClass.getName());
            return keyGenInstance.get();
        } else {
            try {
                LOGGER.trace("Generating CacheKeyGenerator bean from constructor of class [{}]", keyGeneratorClass.getName());
                return keyGeneratorClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                throw new CacheException("No default constructor found in cache key generator ["
                        + keyGeneratorClass.getName() + "]", e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new CacheException("Cache key generator instantiation failed", e);
            }
        }
    }
}
