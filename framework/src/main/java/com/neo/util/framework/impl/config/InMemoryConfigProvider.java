package com.neo.util.framework.impl.config;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigValue;
import com.neo.util.framework.api.config.MutableConfigProvider;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class InMemoryConfigProvider implements MutableConfigProvider {

    protected Map<String, Object> configStore;

    public InMemoryConfigProvider() {
        configStore = new HashMap<>();
    }

    public InMemoryConfigProvider(Map<String, Object> configStore) {
        this.configStore = new HashMap<>(configStore);
    }


    public Config get(String key) {
        return new BasicConfig(key, configStore);
    }

    @Override
    public int getPriorty() {
        return 0;
    }

    public void save(ConfigValue<?> configValue) {
        configStore.put(configValue.key(), configValue.get());
    }

    public <T> ConfigValue<T> newConfig(String key, T value) {
        return new BasicConfigValue<>(key, value);
    }
}
