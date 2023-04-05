package com.neo.util.framework.helidon.impl.config;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigValue;
import io.helidon.config.ConfigValues;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Wraps the hellion specific config implementation onto a generic one {@link Config}
 */
public class HelidonConfigWrapper implements Config {

    protected io.helidon.config.Config config;

    public HelidonConfigWrapper(io.helidon.config.Config config) {
        this.config = config;
    }

    @Override
    public String key() {
        return config.name();
    }

    @Override
    public Config get(String key) {
        return new HelidonConfigWrapper(config.get(key));
    }

    @Override
    public Type type() {
        return switch (config.type()) {
        case VALUE -> Type.VALUE;
            case OBJECT -> Type.OBJECT;
            case LIST -> Type.LIST;
            case MISSING, null -> Type.MISSING;
        };
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
        return new HelidonConfigValueWrapper<>(config.as(clazz));
    }

    @Override
    public <T> ConfigValue<T> as(Function<Config, T> var1) {
        return this.type() == Type.MISSING ?
                new HelidonConfigValueWrapper<>(ConfigValues.empty()) :
                new HelidonConfigValueWrapper<>(ConfigValues.simpleValue(var1.apply(this)));
    }

    @Override
    public ConfigValue<Boolean> asBoolean() {
        return new HelidonConfigValueWrapper<>(config.asBoolean());
    }

    @Override
    public ConfigValue<String> asString() {
        return new HelidonConfigValueWrapper<>(config.asString());
    }

    @Override
    public ConfigValue<Integer> asInt() {
        return new HelidonConfigValueWrapper<>(config.asInt());
    }

    @Override
    public ConfigValue<Long> asLong() {
        return new HelidonConfigValueWrapper<>(config.asLong());
    }

    @Override
    public ConfigValue<Double> asDouble() {
        return new HelidonConfigValueWrapper<>(config.asDouble());
    }

    @Override
    public <T> ConfigValue<List<T>> asList(Class<T> clazz) {
        return new HelidonConfigValueWrapper<>(config.asList(clazz));
    }

    @Override
    public <T> ConfigValue<List<T>> asList(Function<Config, T> mapper) {
        return new HelidonConfigValueWrapper<>(config.asList(node -> mapper.apply(new HelidonConfigWrapper(node))));
    }

    @Override
    public ConfigValue<Map<String, String>> asMap() {
        return new HelidonConfigValueWrapper<>(config.asMap());
    }
}
