package com.neo.util.framework.api.scheduler;

import com.neo.util.common.impl.exception.ExceptionDetails;

/**
 * This interface is an abstraction to the different Scheduler implementations.
 * <p>
 * It defines the necessary functionality which all should implement in one standardized interface.
 */
public interface SchedulerService {

    ExceptionDetails EX_DUPLICATED_SCHEDULER = new ExceptionDetails(
            "queue/duplicated-scheduler-configured","Duplicated scheduler present [{0}].",true);

    ExceptionDetails EX_INVALID_SCHEDULER_ID = new ExceptionDetails(
            "scheduler/invalid-id", "The provided scheduler id [{0}] does not exist.", true);

    ExceptionDetails EX_INVALID_CONFIG_EXPRESSION = new ExceptionDetails(
            "scheduler/invalid-chron", "The configured expression is invalid because [{0}].", true);

    /**
     * Manually executes the provided scheduler
     *
     * @param id the id of the scheduler
     * @throws com.neo.util.common.impl.exception.CommonRuntimeException if id doesn't exist
     */
    void executeScheduler(String id);

    /**
     * Manually starts the scheduler
     *
     * @param id the id of the scheduler
     * @throws com.neo.util.common.impl.exception.CommonRuntimeException if id doesn't exist
     */
    void startScheduler(String id);

    /**
     * Manually stops the scheduler
     *
     * @param id the id of the scheduler
     * @throws com.neo.util.common.impl.exception.CommonRuntimeException if id doesn't exist
     */
    void stopScheduler(String id);

    /**
     * Reload all schedulers from configuration.
     */
    void reload();
}
