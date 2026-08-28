package com.neo.util.framework.api.janitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;

import java.time.Instant;

public class JanitorConfig {

    public static final ExceptionDetails INVALID_JANITOR_ID = new ExceptionDetails("janitor/invalid-key",
            "The janitor key [{0}] may not have whitespaces.");

    protected final String id;
    @JsonIgnore
    protected final JanitorJob janitorJob;

    protected boolean enabled;
    protected Instant lastExecution = Instant.EPOCH;
    protected boolean lastExecutionFailed = false;

    public JanitorConfig(String id, boolean enabled, JanitorJob janitorJob) {
        if (id.contains(" ")) {
            throw new ConfigurationException(INVALID_JANITOR_ID, id);
        }

        this.id = id;
        this.enabled = enabled;
        this.janitorJob = janitorJob;
    }

    public String getId() {
        return id;
    }

    public JanitorJob getJanitorJob() {
        return janitorJob;
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

    public boolean getLastExecutionFailed() {
        return lastExecutionFailed;
    }

    public void setLastExecutionFailed(boolean lastExecutionFailed) {
        this.lastExecutionFailed = lastExecutionFailed;
    }
}
