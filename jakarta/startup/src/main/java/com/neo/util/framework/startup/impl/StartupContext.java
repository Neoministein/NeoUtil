package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.connection.RequestContext;

public record StartupContext(String stage) implements RequestContext {

    @Override
    public String toString() {
        return stage;
    }
}
