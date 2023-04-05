package com.neo.util.framework.impl.config;

import com.neo.util.framework.api.config.ConfigValue;

import java.util.Optional;

public class BasicConfigValue<T> implements ConfigValue<T> {

    protected String key;
    protected T value;

    public BasicConfigValue(String key, T value) {
        this.value = value;
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Optional<T> asOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }
}
