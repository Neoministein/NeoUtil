package com.neo.util.framework.api.request.recording;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.request.RequestDetails;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public abstract class AbstractRequestRecorder<T extends RequestDetails> implements RequestRecorder<T> {

    private static final String REQUEST_RECORDING_PREFIX = "request.recorder.";

    @Inject
    protected ConfigService configService;

    protected boolean enabled;

    @PostConstruct
    protected void postConstruct() {
        this.enabled = configService.get(REQUEST_RECORDING_PREFIX + getRequestType().getSimpleName() + ".enabled").asBoolean().orElse(false);
    }

    @Override
    public boolean enabled() {
        return enabled;
    }
}
