package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.cache.Cache;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class HashMapCache implements Cache {


    protected final Map<Object, Object> cache = new ConcurrentHashMap<>();
    protected final String cacheName;

    public HashMapCache(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public <T> Optional<T> get(Object key) {
        return Optional.ofNullable((T) cache.get(key));
    }

    @Override
    public <K, V> CompletableFuture<Object> getAsync(K key, Function<K, V> valueLoader) {
        return CompletableFuture.supplyAsync(() -> {
            Object cachedValue = cache.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }
            Object computedValue = valueLoader.apply(key);
            cache.put(key, computedValue);
            return computedValue;
        });
    }

    @Override
    public void put(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<Object, Object> map) {
        cache.putAll(map);
    }

    @Override
    public void invalidate(Object key) {
        cache.remove(key);
    }

    @Override
    public CompletableFuture<Void> invalidateAsync(Object key) {
        return CompletableFuture.supplyAsync(() -> {cache.remove(key); return null;});
    }

    @Override
    public void invalidateAll(Iterable<Object> keys) {
        for (Object key: keys) {
            cache.remove(key);
        }
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public CompletableFuture<Void> invalidateAllAsync() {
        invalidateAll();
        return null;
    }

    @Override
    public Set<Object> getAllKeys() {
        return cache.keySet();
    }
}
