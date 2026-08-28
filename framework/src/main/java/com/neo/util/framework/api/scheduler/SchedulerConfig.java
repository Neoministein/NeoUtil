package com.neo.util.framework.api.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;

import java.time.Instant;

public class SchedulerConfig {

    public static final ExceptionDetails INVALID_SCHEDULER_ID = new ExceptionDetails("scheduler/invalid-key",
            "The scheduler key [{0}] may not have whitespaces.");

    public static final ExceptionDetails INVALID_CRON_EXPRESSION = new ExceptionDetails("scheduler/invalid-cron",
            "Invalid cron expression [{0}] for scheduler key [{0}].");
    @JsonIgnore
    protected final RequestContext context;

    protected final String id;

    protected boolean enabled;
    protected String cronValue;
    protected Instant lastExecution = Instant.EPOCH;
    protected boolean lastExecutionFailed = false;

    public SchedulerConfig(String id, boolean enabled, String cronValue) {
        if (id.contains(" ")) {
            throw new ConfigurationException(INVALID_SCHEDULER_ID, id);
        }
        this.context = new SchedulerRequestDetails.Context(id);
        this.id = id;
        this.enabled = enabled;
        this.cronValue = cronValue;
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

    public String getCronValue() {
        return cronValue;
    }

    public void setCronValue(String cronValue) {
        this.cronValue = cronValue;
    }

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

    @Override
    public String toString() {
        return "SchedulerConfig Id: [" + id + "], Enabled: [" + enabled + "], CronValue: [" + cronValue
                + "], LastExecution: [" + lastExecution + "], LastExecutionFailed: [" + lastExecutionFailed + ']';
    }
}