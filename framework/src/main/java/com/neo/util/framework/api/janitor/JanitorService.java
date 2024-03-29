package com.neo.util.framework.api.janitor;

import com.neo.util.framework.api.scheduler.CronSchedule;

import java.util.Collection;

/**
 * This is an interface for a Janitor System to manage {@link JanitorJob} and delete old data.
 */
public interface JanitorService {

    /**
     * Returns a collection of all janitor names.
     *
     * @return names of all janitors
     */
    Collection<String> getJanitorNames();

    /**
     * Executes the {@link JanitorJob} via the {@link JanitorJob#getJanitorId()}
     *
     * @param janitorId to execute
     */
    void execute(String janitorId);

    /**
     * Executes all {@link JanitorJob}
     */
    @CronSchedule(value = "JanitorService", cron = "0 0 * * *")
    void executeAll();

    /**
     * Disables the {@link JanitorJob} via the {@link JanitorJob#getJanitorId()}
     *
     * @param janitorId to disable
     */
    void disableJanitor(String janitorId);
}
