package com.neo.util.helidon.impl.config;

import com.neo.common.impl.exception.InternalLogicException;
import com.neo.javax.api.config.Config;
import com.neo.javax.api.config.ConfigService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
public class ConfigServiceImpl implements ConfigService {

    @Inject
    io.helidon.config.Config config;

    @Override
    public Config empty() {
        throw new InternalLogicException("Retrieving an empty config is not supported in the Helidon implementation");
    }

    @Override
    public Config get(String key) {
        return new ConfigHelidonWrapper(config.get(key));
    }

    @Override
    public void save(Config config) {
        throw new InternalLogicException("Saving config is not supported in the Helidon implementation");
    }
}
