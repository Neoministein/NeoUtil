package com.neo.util.impl.config.store;

import com.neo.util.api.config.ConfigStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EnvConfigStore extends AbstractMapConfigStore implements ConfigStore {

    public EnvConfigStore() {
        super(System.getenv());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void save(String key, String value) {
        throw new UnsupportedOperationException("Cannot save config, EnvConfigStore is an immutable");
    }
}
