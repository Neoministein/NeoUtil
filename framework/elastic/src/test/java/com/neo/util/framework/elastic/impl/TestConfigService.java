package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TestConfigService implements ConfigService {

    protected Map<String, Object> configStore;

    public TestConfigService(Map<String, Object> configStore) {
        this.configStore = new HashMap<>(configStore);
    }

    @Override
    public Config empty() {
        return get("");
    }

    @Override
    public Config get(String key) {
        return new ConfigImpl(key, configStore);
    }

    @Override
    public void save(Config config) {

    }

    public void addConfig(String key, Object object) {
        configStore.put(key, object);
    }

    public class ConfigImpl implements Config {

        protected final String key;
        protected final Map<String, Object> configStore;

        public ConfigImpl(String key, Map<String, Object> configStore) {
            this.key = key;
            this.configStore = configStore;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Config get(String key) {
            return new ConfigImpl(this.key + "." + key, configStore);
        }

        @Override
        public Type type() {
            return null;
        }

        @Override
        public boolean hasValue() {
            return configStore.containsKey(key);
        }

        @Override
        public <T> ConfigValue<T> as(Class<T> clazz) {
            return new ConfigValueImpl<>(key, clazz.cast(configStore.get(key)));
        }

        @Override
        public <T> ConfigValue<T> as(Function<Config, T> var1) {
            return new ConfigValueImpl<>("", var1.apply(this));
        }

        @Override
        public <T> ConfigValue<List<T>> asList(Class<T> var1) {
            return new ConfigValueImpl<List<T>>(key, List.class.cast(configStore.get(key)));
        }

        @Override
        public ConfigValue<Map<String, String>> asMap() {
            return null;
        }

        @Override
        public void put(Config config) {
            throw new IllegalArgumentException("Should not be called");
        }

        @Override
        public <T> void set(ConfigValue<T> configValue) {
            throw new IllegalArgumentException("Should not be called");
        }
    }

    public class ConfigValueImpl<T> implements ConfigValue<T> {

        protected String key;
        protected T value;

        public ConfigValueImpl(String key, T value) {
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
    }
}
