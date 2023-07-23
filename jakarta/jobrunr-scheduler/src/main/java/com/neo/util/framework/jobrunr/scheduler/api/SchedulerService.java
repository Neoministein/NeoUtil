package com.neo.util.framework.jobrunr.scheduler.api;

import com.neo.util.common.impl.exception.ExceptionDetails;

public interface SchedulerService {

    ExceptionDetails EX_INVALID_SCHEDULER_ID = new ExceptionDetails(
            "scheduler/invalid-id", "The provided scheduler id [{0}] does not exist.", true);

    ExceptionDetails EX_INVALID_CHRON_EXPRESSION = new ExceptionDetails(
            "scheduler/invalid-chron", "The chron expression is invalid because [{0}].", true);

    void executeScheduler(String id);

    void stopScheduler(String id);

    void startScheduler(String id);

    void reload();
}
