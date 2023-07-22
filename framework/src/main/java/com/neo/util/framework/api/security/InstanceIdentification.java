package com.neo.util.framework.api.security;

import com.neo.util.framework.api.PriorityConstants;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import org.slf4j.MDC;

import java.util.UUID;

@ApplicationScoped
public class InstanceIdentification {

    public String id;

    public void init(@Observes @Priority( PriorityConstants.PLATFORM_BEFORE ) @Initialized( ApplicationScoped.class ) Object init ) {
        this.id = UUID.randomUUID().toString();
        MDC.put("instance", id);
    }

    public String getId() {
        return id;
    }
}
