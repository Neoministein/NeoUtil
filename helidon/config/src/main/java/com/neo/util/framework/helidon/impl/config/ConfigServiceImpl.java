package com.neo.util.framework.helidon.impl.config;

import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;

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
