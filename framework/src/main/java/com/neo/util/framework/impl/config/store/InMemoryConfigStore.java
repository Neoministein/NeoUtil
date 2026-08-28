package com.neo.util.framework.impl.config.store;

import com.neo.util.framework.api.config.ConfigStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class InMemoryConfigStore extends AbstractMapConfigStore implements ConfigStore {

    public InMemoryConfigStore() {
        super(new HashMap<>());
    }

    public InMemoryConfigStore(Map<String, String> configValues) {
        super(new HashMap<>(configValues));
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
