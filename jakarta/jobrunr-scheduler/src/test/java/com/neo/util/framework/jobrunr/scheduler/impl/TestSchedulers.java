package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TestSchedulers implements InterfaceWithSchedule {

    int intervalExecutionCount = 0;
    int cronExecutionCount = 0;
    int interfaceExecutionCount = 0;

    @FixedRateSchedule(value = "interval", delay = 10, timeUnit = TimeUnit.SECONDS)
    protected void interval() {
        intervalExecutionCount++;
    }

    @CronSchedule(value = "cron", cron = "0/5 * * * * *")
    protected void cron() {
        cronExecutionCount++;
    }

    @Override
    public void interfaceMethod() {
        interfaceExecutionCount++;
    }

    public int getIntervalExecutionCount() {
        return intervalExecutionCount;
    }

    public int getCronExecutionCount() {
        return cronExecutionCount;
    }

    public int getInterfaceExecutionCount() {
        return interfaceExecutionCount;
    }
}
