package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.framework.api.scheduler.FixedRateSchedule;

import java.util.concurrent.TimeUnit;

public interface InterfaceWithSchedule {

    @FixedRateSchedule(value = "interface", delay = 5, timeUnit = TimeUnit.SECONDS)
    void interfaceMethod();
}
