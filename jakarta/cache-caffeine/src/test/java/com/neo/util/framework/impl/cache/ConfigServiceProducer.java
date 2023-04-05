package com.neo.util.framework.impl.cache;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.impl.config.BasicConfigService;
import jakarta.enterprise.inject.Produces;

import java.util.HashMap;
import java.util.Map;

public class ConfigServiceProducer {

    protected Map<String, Object> configMap = new HashMap<>();

    @Produces
    public ConfigService get() {
        return new BasicConfigService(configMap);
    }

    public void setConfigMap(Map<String, Object> configMap) {
        this.configMap = configMap;
    }
}
