package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.framework.api.scheduler.CronSchedule;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestSchedulers implements InterfaceWithSchedule {

    int cronExecutionCount = 0;
    int interfaceExecutionCount = 0;

    @CronSchedule(value = "cron", cron = "*/5 * * * * *")
    void cron() {
        cronExecutionCount++;
    }

    @Override
    public void interfaceMethod() {
        interfaceExecutionCount++;
    }

    public int getCronExecutionCount() {
        return cronExecutionCount;
    }

    public int getInterfaceExecutionCount() {
        return interfaceExecutionCount;
    }
}
