package com.neo.util.framework.impl.security;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.security.InstanceIdentification;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import org.slf4j.MDC;

import java.util.UUID;

@ApplicationScoped
public class BasicInstanceIdentification implements InstanceIdentification {

    private String id;

    public void init(@Observes @Priority( PriorityConstants.PLATFORM_BEFORE ) @Initialized( ApplicationScoped.class ) Object init ) {
        this.id = UUID.randomUUID().toString();
        MDC.put(MDC_INSTANCE, id);
    }

    public String getInstanceId() {
        return id;
    }
}
