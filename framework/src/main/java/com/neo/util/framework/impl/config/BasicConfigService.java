package com.neo.util.framework.impl.config;

import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;

import java.util.HashMap;
import java.util.Map;

public class BasicConfigService implements ConfigService {
    protected Map<String, Object> configStore;

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
}
