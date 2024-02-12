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

    ExceptionDetails EX_METHOD_NOT_ACCESSIBLE = new ExceptionDetails(
            "scheduler/method-not-accessible", "Scheduler is not accessible [{0}.{1}].", true);
    ExceptionDetails EX_DUPLICATED_SCHEDULER = new ExceptionDetails(
            "scheduler/duplicated-scheduler-configured","Duplicated scheduler present [{0}].",true);

    ExceptionDetails EX_INVALID_SCHEDULER_ID = new ExceptionDetails(
            "scheduler/invalid-id", "The provided scheduler id [{0}] does not exist.", true);

    ExceptionDetails EX_INVALID_CONFIG_EXPRESSION = new ExceptionDetails(
            "scheduler/invalid-chron", "The configured expression is invalid because [{0}].", true);

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
    SchedulerConfig getSchedulerConfig(String schedulerId) throws NoContentFoundException;

    /**
     * Reload all schedulers from configuration.
     */
    void reload();

    /**
     * Returns a collection of all scheduler ids.
     */
    Set<String> getSchedulerIds();
}
