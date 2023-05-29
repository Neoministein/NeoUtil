package com.neo.util.framework.helidon.impl.config;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

@Alternative
@Priority(PriorityConstants.APPLICATION)
@ApplicationScoped
public class ConfigServiceImpl implements ConfigService {

    @Inject
    protected io.helidon.config.Config config;

    @Override
    public Config get(String key) {
        return new HelidonConfigWrapper(config.get(key));
    }

    @Override
    public void save(ConfigValue<?> config) {
        throw new IllegalArgumentException("Saving config is not supported in the Helidon implementation");
    }
}
