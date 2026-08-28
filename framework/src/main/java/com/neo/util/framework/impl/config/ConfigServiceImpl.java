package com.neo.util.framework.impl.config;

import com.neo.util.common.impl.exception.InternalRuntimeException;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigStore;
import com.neo.util.framework.api.config.ConfigValue;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@ApplicationScoped
public class ConfigServiceImpl implements ConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);

    protected final List<ConfigStore> providers;

    public void applicationReadyEvent(@Observes ApplicationPreReadyEvent applicationPostReadyEvent) {
        LOGGER.debug("ApplicationPostReadyEvent processed");
    }

    @Inject
    public ConfigServiceImpl(Instance<ConfigStore> storeInstance) {
        this(storeInstance.stream());
    }

    public ConfigServiceImpl(Collection<ConfigStore> configStores) {
        this(configStores.stream());
    }

    private ConfigServiceImpl(Stream<ConfigStore> configStoreStream) {
        LOGGER.info("Registering ConfigProvider...");
        List<ConfigStore> providers = new ArrayList<>();
        for (ConfigStore provider: configStoreStream.sorted().toList()) {

            providers.add(provider);
            LOGGER.debug("Registered ConfigProvider [{}] with priority [{}]", provider.getClass().getSimpleName(),
                    provider.getPriority());
        }
        this.providers = providers;
    }

    @Override
    public void save(Object value, String... keys) {
        if (keys.length == 0) {
            return;
        }
        String key = String.join(".", keys);
        for (ConfigStore configProvider: providers) {
            if (configProvider.isMutable()) {
                configProvider.save(key, value.toString());
                return;
            }
        }
    }

    @Override
    public <T> ConfigValue<T> getAs(Class<T> clazz, Function<String, T> mapper, String... keys) {
        String key = String.join(".", keys);
        for (ConfigStore configProvider: providers) {
            Optional<String> value = configProvider.get(key);
            if (value.isPresent()) {
                return new BasicConfigValue<>(key, value.map(cast(key, clazz, mapper)));
            }
        }
        return new BasicConfigValue<>(key, Optional.empty());
    }

    @Override
    public <T> ConfigValue<List<T>> getAsList(Class<T> clazz, Function<String, T> mapper, String... keys) {
        String key = String.join(".", keys);
        Function<String, T> cast = cast(key, clazz, mapper);
        for (ConfigStore configProvider: providers) {
            Optional<List<String>> value = configProvider.getList(key);
            if (value.isPresent()) {
                return new BasicConfigValue<>(key, value.map(list -> list.stream().map(cast).toList()));
            }
        }
        return new BasicConfigValue<>(key, Optional.empty());
    }

    protected <T> Function<String, T> cast(String key, Class<T> clazz, Function<String, T> cast) {
        return x -> {
            try {
                return clazz.cast(cast.apply(x));
            } catch (RuntimeException ex) {
                throw new InternalRuntimeException(ex, EX_CONFIG_PARSING, key, x, clazz.getSimpleName());
            }
        };
    }

    private record BasicConfigValue<T>(
            String key,
            Optional<T> value) implements ConfigValue<T> {

        @Override
        public Optional<T> asOptional() {
            return value;
        }
    }
}
