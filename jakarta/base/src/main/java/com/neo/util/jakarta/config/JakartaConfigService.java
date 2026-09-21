package com.neo.util.jakarta.config;

import com.neo.util.api.config.ConfigStore;
import com.neo.util.api.config.DefaultConfigService;
import com.neo.util.api.event.ApplicationPreReadyEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class JakartaConfigService extends DefaultConfigService {

    protected void postReadyEvent(@Observes ApplicationPreReadyEvent applicationPostReadyEvent) {
        LOGGER.debug("Post Ready Event received");
    }

    @Inject
    public JakartaConfigService(Instance<ConfigStore> configStoreStream) {
        super(configStoreStream.stream());
    }
}
