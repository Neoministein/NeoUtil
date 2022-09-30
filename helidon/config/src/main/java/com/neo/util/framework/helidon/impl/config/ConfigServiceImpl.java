package com.neo.util.framework.helidon.impl.config;

import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConfigServiceImpl implements ConfigService {

    @Inject
    protected io.helidon.config.Config config;

    @Override
    public Config get(String key) {
        return new ConfigHelidonWrapper(config.get(key));
    }

    @Override
    public void save(ConfigValue<?> config) {
        throw new InternalLogicException("Saving config is not supported in the Helidon implementation");
    }
}
