package com.neo.util.jakarta.database.config;

import com.neo.util.api.config.ConfigStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DatabaseConfigStore implements ConfigStore {

    protected final ConfigValueEntityRepository configValueEntityRepository;

    @Inject
    public DatabaseConfigStore(ConfigValueEntityRepository configValueEntityRepository) {
        this.configValueEntityRepository = configValueEntityRepository;
    }

    @Override
    public Optional<String> get(String key) {
        return configValueEntityRepository.fetch(key).map(ConfigValueEntity::getValue);
    }

    @Override
    public Optional<List<String>> getList(String key) {
        List<String> list = new ArrayList<>();

        int index = 0;
        while (true) {
            Optional<ConfigValueEntity> entity = configValueEntityRepository.fetch(key + "." + index++);
            if (entity.isEmpty()) {
                break;
            }
            list.add(entity.get().getValue());
        }
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list);
    }

    @Override
    @Transactional
    public void save(String key, String value) {
        Optional<ConfigValueEntity> configValue = configValueEntityRepository.fetch(key);
        if (configValue.isPresent()) {
            configValue.get().setValue(value);
            return;
        }

        ConfigValueEntity newConfigValue = new ConfigValueEntity(key, value);
        newConfigValue.setValue(value);
        configValueEntityRepository.create(newConfigValue);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
