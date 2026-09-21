package com.neo.util.jakarta.jobrunr.scheduler;

import com.neo.util.api.scheduler.CronSchedule;

public interface InterfaceWithSchedule {

    @CronSchedule(value = "interface", cron = "*/2 * * * * *")
    void interfaceMethod();
}
