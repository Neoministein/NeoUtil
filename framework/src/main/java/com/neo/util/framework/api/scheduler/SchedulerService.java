package com.neo.util.framework.api.scheduler;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;

import java.util.Set;

/**
 * This interface is an abstraction to the different Scheduler implementations.
 * <p>
 * It defines the necessary functionality which all should implement in one standardized interface.
 */
public interface SchedulerService {

    ExceptionDetails EX_DUPLICATED_SCHEDULER = new ExceptionDetails(
            "scheduler/duplicated-scheduler-configured","Duplicated scheduler present [{0}].");

    String E_INVALID_SCHEDULER_ID = "scheduler/invalid-id";

    ExceptionDetails EX_INVALID_SCHEDULER_ID = new ExceptionDetails(
            E_INVALID_SCHEDULER_ID, "The provided scheduler id [{0}] does not exist.");

    ExceptionDetails EX_INVALID_CONFIG_EXPRESSION = new ExceptionDetails(
            "scheduler/invalid-chron", "The configured expression is invalid because [{0}].");

    /**
     * Manually executes the provided scheduler
     *
     * @param schedulerId the id of the scheduler
     */
    void execute(String schedulerId) throws NoContentFoundException;

    /**
     * Manually starts the scheduler
     *
     * @param schedulerId the id of the scheduler
     */
    void start(String schedulerId) throws NoContentFoundException;

    /**
     * Manually stops the scheduler
     *
     * @param schedulerId the id of the scheduler
     */
    void stop(String schedulerId) throws NoContentFoundException;

    /**
     * Returns the config for the provided scheduler name
     */
    SchedulerConfig requestSchedulerConfig(String schedulerId) throws NoContentFoundException;

    /**
     * Reload all schedulers from configuration.
     */
    void reload();

    /**
     * Returns a collection of all scheduler ids.
     */
    Set<String> getSchedulerIds();
}
