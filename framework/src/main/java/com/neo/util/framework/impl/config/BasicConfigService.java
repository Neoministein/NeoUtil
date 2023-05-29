package com.neo.util.framework.impl.config;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class BasicConfigService implements ConfigService {
    protected Map<String, Object> configStore;

    public BasicConfigService() {
        configStore = new HashMap<>();
    }

    public BasicConfigService(Map<String, Object> configStore) {
        this.configStore = new HashMap<>(configStore);
    }

    @Override
    public Config get(String key) {
        return new BasicConfig(key, configStore);
    }

    @Override
    public void save(ConfigValue<?> configValue) {
        configStore.put(configValue.key(), configValue.get());
    }

    @Override
    public <T> ConfigValue<T> newConfig(String key, T value) {
        return new BasicConfigValue<>(key, value);
    }
}
