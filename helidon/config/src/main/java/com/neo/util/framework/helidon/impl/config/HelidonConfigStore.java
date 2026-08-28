package com.neo.util.framework.helidon.impl.config;

import com.neo.util.framework.api.config.ConfigStore;
import io.helidon.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HelidonConfigStore implements ConfigStore {

    protected final Config config;

    @Inject
    public HelidonConfigStore(Config config) {
        this.config = config;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Optional<String> get(String key) {
        return config.get(key).asString().asOptional();
    }

    @Override
    public Optional<List<String>> getList(String key) {
        return config.get(key).asList(String.class).asOptional();
    }

    @Override
    public void save(String key, String value) {
        throw new UnsupportedOperationException("Cannot save config HelidonConfigProvider is an immutable");
    }
}
