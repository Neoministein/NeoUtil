package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.request.RequestContext;

public record StartupContext(String stage) implements RequestContext {

    @Override
    public String type() {
        return "Startup";
    }

    @Override
    public String toString() {
        return stage;
    }
}
