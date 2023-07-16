package com.neo.util.framework.caffeine.impl;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.neo.util.framework.api.cache.Cache;
import com.neo.util.framework.impl.cache.spi.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CaffeineCache implements Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineCache.class);

    protected final String cacheName;

    protected final AsyncCache<Object, Object> cache;

    public CaffeineCache(CaffeineCacheConfig caffeineCacheInfo) {
        cacheName = caffeineCacheInfo.cacheName();

        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        caffeineCacheInfo.expireAfterSeconds().ifPresent(val ->  builder.expireAfterAccess(val, TimeUnit.SECONDS));
        caffeineCacheInfo.expireAfterSeconds().ifPresent(val ->  builder.expireAfterWrite(val, TimeUnit.SECONDS));
        caffeineCacheInfo.initialCapacity().ifPresent(builder::initialCapacity);
        caffeineCacheInfo.maxCapacity().ifPresent(builder::maximumSize);

        cache = builder.buildAsync();
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public <T> Optional<T> get(Object key) {
        LOGGER.trace("Hitting caffeine cache with key [{}]", key);
        CompletableFuture<Object> future = cache.getIfPresent(key);
        if (future == null) {
            return Optional.empty();
        }
        try {
            return (Optional<T>) Optional.ofNullable(unwrapCacheValueOrThrowable(future).get());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ClassCastException ex) {
            LOGGER.warn("Cannot cast for key [{}]", key);
        } catch (ExecutionException ignored) {}


        return Optional.empty();
    }

    public <K, V> CompletableFuture<Object> getAsync(K key, Function<K, V> valueLoader) {
        LOGGER.trace("Hitting async caffeine cache with key [{}]", key);
        return getFromCaffeine(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        LOGGER.trace("Caching value [{}] for key [{}]", value,  key);
        cache.put(key, CompletableFuture.completedFuture(value));
    }

    @Override
    public void putAll(Map<Object, Object> map) {
        LOGGER.trace("Bulk caching values for keys [{}]", map.keySet());
        cache.synchronous().putAll(map);
    }

    @Override
    public void invalidate(Object key) {
        LOGGER.trace("Invalidating cache [{}] for key [{}]",cacheName, key);
        cache.synchronous().invalidate(key);
    }

    @Override
    public CompletableFuture<Void> invalidateAsync(Object key) {
        return CompletableFuture.runAsync(() -> invalidate(key));
    }

    @Override
    public void invalidateAll(Iterable<Object> keys) {
        LOGGER.trace("Bulk invalidating cache [{}] for keys [{}]", cacheName, keys);
        cache.synchronous().invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        LOGGER.info("Invalidating entire cache [{}]", cacheName);
        cache.synchronous().invalidateAll();
    }

    @Override
    public CompletableFuture<Void> invalidateAllAsync() {
        return CompletableFuture.runAsync(this::invalidateAll);
    }

    @Override
    public Set<Object> getAllKeys() {
        return cache.asMap().keySet();
    }

    /**
     * Returns a {@link CompletableFuture} holding the cache value identified by {@code key}, obtaining that value from
     * {@code valueLoader} if necessary. The value computation is done synchronously on the calling thread and the
     * {@link CompletableFuture} is immediately completed before being returned.
     *
     * @param key cache key
     * @param valueLoader function used to compute the cache value if {@code key} is not already associated with a value
     * @return a {@link CompletableFuture} holding the cache value
     * @throws CacheException if an exception is thrown during the cache value computation
     */
    protected  <K, V> CompletableFuture<Object> getFromCaffeine(K key, Function<K, V> valueLoader) {
        CompletableFuture<Object> newCacheValue = new CompletableFuture<>();
        CompletableFuture<Object> existingCacheValue = cache.asMap().putIfAbsent(key, newCacheValue);
        if (existingCacheValue == null) {
            try {
                newCacheValue.complete(valueLoader.apply(key));
            } catch (Exception ex) {
                newCacheValue.complete(new CaffeineComputationException(ex));
            } catch (Throwable ex) {
                cache.asMap().remove(key, newCacheValue);
                newCacheValue.complete(new CaffeineComputationException(ex));
            }
            return unwrapCacheValueOrThrowable(newCacheValue);
        } else {
            LOGGER.trace("Key [{}] found in cache [{}]", key, cacheName);
            return unwrapCacheValueOrThrowable(existingCacheValue);
        }
    }

    protected CompletableFuture<Object> unwrapCacheValueOrThrowable(CompletableFuture<Object> cacheValue) {
        return cacheValue.thenApply(value -> {
            // If there's a throwable encapsulated into a CaffeineComputationThrowable, it must be rethrown.
            if (value instanceof CaffeineComputationException computationException) {
                Throwable cause = computationException.cause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                } else if (cause instanceof Error error) {
                    throw error;
                } else {
                    //This should never be executed
                    throw new CacheException(cause);
                }
            } else {
                return value;
            }
        });
    }
}
