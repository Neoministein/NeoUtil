package com.neo.util.framework.impl.config;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BasicConfig implements Config {

    protected final String key;
    protected final Map<String, Object> configStore;

    public BasicConfig(String key, Map<String, Object> configStore) {
        this.key = key;
        this.configStore = configStore;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Config get(String key) {
        return new BasicConfig(this.key + "." + key, configStore);
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
        return new BasicConfigValue<>(key, clazz.cast(configStore.get(key)));
    }

    @Override
    public <T> ConfigValue<T> as(Function<Config, T> var1) {
        return new BasicConfigValue<>("", var1.apply(this));
    }

    @Override
    public <T> ConfigValue<List<T>> asList(Class<T> clazz) {
        return new BasicConfigValue<List<T>>(key, List.class.cast(configStore.get(key)));
    }

    @Override
    public ConfigValue<Map<String, String>> asMap() {
        return null;
    }

    @Override
    public <T> ConfigValue<List<T>> asList(Function<Config, T> mapper) {
        List<T> list = new ArrayList<>();


        for (Map.Entry<String, Object> entry: configStore.entrySet()) {
            if (isChildNodeOfCurrentInstance(entry.getKey())) {

                list.add(mapper.apply(new BasicConfig(entry.getKey(), configStore)));
            }
        }
        return new BasicConfigValue<>(key, list);
    }

    protected boolean isChildNodeOfCurrentInstance(String nodeKey) {
        return nodeKey.startsWith(key) &&
                StringUtils.countMatches(nodeKey, '.') + 1 == StringUtils.countMatches(key, '.');
    }
}
