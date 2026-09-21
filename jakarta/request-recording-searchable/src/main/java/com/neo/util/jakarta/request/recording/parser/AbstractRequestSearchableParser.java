package com.neo.util.jakarta.request.recording.parser;

import com.neo.util.api.config.ConfigService;
import com.neo.util.api.request.RequestDetails;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public abstract class AbstractRequestSearchableParser<T extends RequestDetails> implements RequestSearchableParser<T> {

    @Inject
    protected ConfigService configService;

    protected boolean enabled;

    @PostConstruct
    protected void postConstruct() {
        this.enabled = configService.getAsBoolean("request.recorder", getRequestType().getSimpleName(), "enabled").orElse(false);
    }

    @Override
    public boolean enabled() {
        return enabled;
    }
}
