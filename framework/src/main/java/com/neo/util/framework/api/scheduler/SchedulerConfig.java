package com.neo.util.framework.api.scheduler;

import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;

import java.time.Instant;

public class SchedulerConfig {

    protected final String id;
    protected final RequestContext context;

    protected boolean enabled;
    protected Instant lastExecution = null;
    protected Boolean lastExecutionFailed = null;

    public SchedulerConfig(String id, boolean enabled) {
        this.id = id;
        this.enabled = enabled;
        this.context = new SchedulerRequestDetails.Context(id);
    }

    public String getId() {
        return id;
    }


    public RequestContext getContext() {
        return context;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Instant lastExecution) {
        this.lastExecution = lastExecution;
    }

    public Boolean getLastExecutionFailed() {
        return lastExecutionFailed;
    }

    public void setLastExecutionFailed(Boolean lastExecutionFailed) {
        this.lastExecutionFailed = lastExecutionFailed;
    }
}
