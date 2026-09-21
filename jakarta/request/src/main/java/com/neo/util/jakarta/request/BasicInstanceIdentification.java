package com.neo.util.jakarta.request;

import com.neo.util.common.api.PriorityConstants;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import org.slf4j.MDC;

import java.util.UUID;

@ApplicationScoped
public class BasicInstanceIdentification implements InstanceIdentification {

    private final String id;

    public BasicInstanceIdentification() {
        this.id = UUID.randomUUID().toString();
    }

    public void init(@Observes @Priority( PriorityConstants.PLATFORM_BEFORE ) @Initialized( ApplicationScoped.class ) Object init ) {
        MDC.put(MDC_INSTANCE, id);
    }

    public String getInstanceId() {
        return id;
    }
}
