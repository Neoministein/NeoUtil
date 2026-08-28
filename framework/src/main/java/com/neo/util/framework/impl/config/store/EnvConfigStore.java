package com.neo.util.framework.impl.config.store;

import com.neo.util.framework.api.config.ConfigStore;
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
