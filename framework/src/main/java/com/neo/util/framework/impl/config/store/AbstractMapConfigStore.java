package com.neo.util.framework.impl.config.store;

import com.neo.util.framework.api.config.ConfigStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractMapConfigStore implements ConfigStore {

    protected final Map<String, String> configStore;

    public AbstractMapConfigStore(Map<String, String> configStore) {
        this.configStore = configStore;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(configStore.get(key));
    }

    @Override
    public Optional<List<String>> getList(String key) {
        List<String> list = new ArrayList<>();

        int index = 0;
        while (true) {
            String value = configStore.get(key + "." + index++);
            if (value == null) {
                break;
            }
            list.add(value);
        }
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list);
    }

    @Override
    public void save(String key, String value) {
        configStore.put(key, value);
    }
}
