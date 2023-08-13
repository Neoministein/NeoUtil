package com.neo.util.framework.jobrunr.scheduler.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neo.util.framework.api.scheduler.SchedulerConfig;
import org.jobrunr.scheduling.Schedule;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class JobRunnerSchedulerConfig extends SchedulerConfig {

    @JsonIgnore
    protected final Method method;
    //This is done since the creation time of the object is important to how the calculation works
    @JsonIgnore
    protected final Supplier<Schedule> schedule;

    @JsonIgnore
    protected Object beanInstance;


    public JobRunnerSchedulerConfig(String id, Method method, Supplier<Schedule> schedule) {
        super(id, false);
        this.method = method;
        this.schedule = schedule;
    }

    public Method getMethod() {
        return method;
    }

    public Supplier<Schedule> getSchedule() {
        return schedule;
    }


    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }
}
