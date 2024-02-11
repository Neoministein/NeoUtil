package com.neo.util.framework.impl.scheduler;

import com.neo.util.framework.api.scheduler.SchedulerConfig;
import com.neo.util.framework.api.scheduler.SchedulerService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class DummySchedulerService implements SchedulerService {

    @Override
    public SchedulerConfig getSchedulerConfig(String id) {
        return new SchedulerConfig("TestScheduler", false);
    }

    @Override
    public void executeScheduler(String id) {

    }

    @Override
    public void startScheduler(String id) {

    }

    @Override
    public void stopScheduler(String id) {

    }

    @Override
    public void reload() {

    }

    @Override
    public Set<String> getSchedulerIds() {
        return Set.of("");
    }
}
