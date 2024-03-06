package com.neo.util.framework.api.scheduler;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;
import jakarta.annotation.Nullable;

import java.time.Instant;

public class SchedulerConfig {

    public static final ExceptionDetails INVALID_SCHEDULER_ID = new ExceptionDetails(
            "scheduler/invalid-id", "The scheduler id [{0}] may not have whitespaces.");

    protected final String id;
    protected final RequestContext context;

    protected boolean enabled;
    protected Instant lastExecution = null;
    protected boolean lastExecutionFailed = false;

    public SchedulerConfig(String id, boolean enabled) {
        if (id.contains(" ")) {
            throw new ConfigurationException(INVALID_SCHEDULER_ID, id);
        }

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

    @Nullable
    public Instant getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Instant lastExecution) {
        this.lastExecution = lastExecution;
    }

    public boolean getLastExecutionFailed() {
        return lastExecutionFailed;
    }

    public void setLastExecutionFailed(boolean lastExecutionFailed) {
        this.lastExecutionFailed = lastExecutionFailed;
    }
}
