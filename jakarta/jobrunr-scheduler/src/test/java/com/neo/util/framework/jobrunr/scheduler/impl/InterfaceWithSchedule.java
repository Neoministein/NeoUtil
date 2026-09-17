package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.api.scheduler.CronSchedule;

public interface InterfaceWithSchedule {

    @CronSchedule(value = "interface", cron = "*/2 * * * * *")
    void interfaceMethod();
}
