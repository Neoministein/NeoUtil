package com.neo.util.framework.api.janitor;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.framework.api.scheduler.CronSchedule;

import java.util.Set;

/**
 * This is an interface for a Janitor System to manage {@link JanitorJob} and delete old data.
 */
public interface JanitorService {

    String E_NON_EXISTENT_JANITOR_JOB = "janitor/invalid-id";

    ExceptionDetails EX_NON_EXISTENT_JANITOR_JOB = new ExceptionDetails(
            E_NON_EXISTENT_JANITOR_JOB, "The Janitor [{0}] does not exist");

    /**
     * Executes all {@link JanitorJob}
     */
    @CronSchedule(value = "JanitorService", cron = "0 0 * * *")
    void executeAll();

    /**
     * Executes the {@link JanitorJob} via the {@link JanitorJob#getJanitorId()}
     *
     * @param janitorId to execute
     */
    void execute(String janitorId) throws NoContentFoundException;

    /**
     * Enables the {@link JanitorJob} via the {@link JanitorJob#getJanitorId()}
     *
     * @param janitorId to enable
     */
    void enable(String janitorId) throws NoContentFoundException;

    /**
     * Disables the {@link JanitorJob} via the {@link JanitorJob#getJanitorId()}
     *
     * @param janitorId to disable
     */
    void disable(String janitorId) throws NoContentFoundException;

    /**
     * Returns the config for the provided janitor id
     */
    JanitorConfig requestJanitorConfig(String janitorId) throws NoContentFoundException;

    /**
     * Returns a collection of all janitor ids.
     *
     * @return ids of all janitors
     */
    Set<String> fetchJanitorIds();
}
