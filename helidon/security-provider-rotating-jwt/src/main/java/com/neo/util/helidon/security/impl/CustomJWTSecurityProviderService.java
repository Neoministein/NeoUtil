package com.neo.util.helidon.security.impl;

import io.helidon.common.config.Config;
import io.helidon.security.spi.SecurityProvider;
import io.helidon.security.spi.SecurityProviderService;

public class CustomJWTSecurityProviderService implements SecurityProviderService {

    @Override
    public String providerConfigKey() {
        return "custom-jwt";
    }

    @Override
    public Class<? extends SecurityProvider> providerClass() {
        return CustomJWTAuthentication.class;
    }

    @Override
    public SecurityProvider providerInstance(Config config) {
        return new CustomJWTAuthentication(config);
    }
}
