package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TestSchedulers {

    int intervalExecutionCount = 0;
    int cronExecutionCount = 0;

    @FixedRateSchedule(value = "interval", delay = 10, timeUnit = TimeUnit.SECONDS)
    protected void interval() {
        intervalExecutionCount++;
    }

    @CronSchedule(value = "cron", cron = "0/5 * * * * *")
    protected void cron() {
        cronExecutionCount++;
    }

    public int getIntervalExecutionCount() {
        return intervalExecutionCount;
    }

    public int getCronExecutionCount() {
        return cronExecutionCount;
    }
}
