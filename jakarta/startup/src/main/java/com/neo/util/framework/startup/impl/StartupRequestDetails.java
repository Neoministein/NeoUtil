package com.neo.util.framework.startup.impl;

import com.neo.util.framework.api.request.AbstractRequestDetails;
import com.neo.util.framework.api.request.RequestContext;

public class StartupRequestDetails extends AbstractRequestDetails {

    public StartupRequestDetails(String instanceId, String stage) {
        super(instanceId, new Context(stage));
    }

    @Override
    public String getInitiator() {
        return requestContext.toString();
    }

    public record Context(String stage) implements RequestContext {

        @Override
        public String type() {
            return "Startup";
        }

        @Override
        public String toString() {
            return stage;
        }
    }
}
