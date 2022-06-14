package com.neo.util.framework.helidon.impl.config;

import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigValue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Wraps the hellion specific config implementation onto a generic one {@link Config}
 */
public class ConfigHelidonWrapper implements Config {

    protected io.helidon.config.Config config;

    public ConfigHelidonWrapper(io.helidon.config.Config config) {
        this.config = config;
    }

    @Override
    public String key() {
        return config.name();
    }

    @Override
    public Config get(String key) {
        return new ConfigHelidonWrapper(config.get(key));
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public boolean exists() {
        return config.exists();
    }

    @Override
    public boolean isLeaf() {
        return config.isLeaf();
    }

    @Override
    public boolean hasValue() {
        return config.hasValue();
    }

    @Override
    public <T> ConfigValue<T> as(Class<T> clazz) {
        return as(clazz);
    }

    @Override
    public <T> ConfigValue<T> as(Function<Config, T> var1) {
        return as(var1);
    }

    @Override
    public ConfigValue<Boolean> asBoolean() {
        return new ConfigValueHelidonWrapper<>(config.asBoolean());
    }

    @Override
    public ConfigValue<String> asString() {
        return new ConfigValueHelidonWrapper<>(config.asString());
    }

    @Override
    public ConfigValue<Integer> asInt() {
        return new ConfigValueHelidonWrapper<>(config.asInt());
    }

    @Override
    public ConfigValue<Long> asLong() {
        return new ConfigValueHelidonWrapper<>(config.asLong());
    }

    @Override
    public ConfigValue<Double> asDouble() {
        return new ConfigValueHelidonWrapper<>(config.asDouble());
    }

    @Override
    public <T> ConfigValue<List<T>> asList(Class<T> var1) {
        return new ConfigValueHelidonWrapper<>(config.asList(var1));
    }

    @Override
    public ConfigValue<Map<String, String>> asMap() {
        return new ConfigValueHelidonWrapper<>(config.asMap());
    }

    @Override
    public void put(Config config) {
        throw new InternalLogicException("Adding config is not supported in the Helion implementation");
    }

    @Override
    public <T> void set(ConfigValue<T> configValue) {
        throw new InternalLogicException("Setting config is not supported in the Helion implementation");
    }
}
