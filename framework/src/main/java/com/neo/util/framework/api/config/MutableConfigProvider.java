package com.neo.util.framework.api.config;

public interface MutableConfigProvider extends ConfigProvider {

    /**
     * Persists the given {@link ConfigValue}.
     */
    void save(ConfigValue<?> configValue);

    /**
     * Creates a new instance of {@link ConfigValue<T>} which hasn't been saved
     *
     * @param key the key of the config
     * @param value the config value
     *
     * @return a new instance of {@link ConfigValue<T>}
     *
     * @param <T> the type of value
     */
    <T> ConfigValue<T> newConfig(String key, T value);
}
