package com.neo.util.framework.jobrunr.scheduler.api;

import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;
import org.jobrunr.scheduling.Schedule;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class SchedulerConfig {

    protected final String id;
    protected final Method method;
    //This is done since the creation time of the object is important to how the calculation works
    protected final Supplier<Schedule> schedule;

    protected final RequestContext context;
    protected Object beanInstance;

    public SchedulerConfig(String id, Method method, Supplier<Schedule> schedule) {
        this.id = id;
        this.method = method;
        this.schedule = schedule;
        this.context = new SchedulerRequestDetails.Context(id);
    }

    public String getId() {
        return id;
    }

    public Method getMethod() {
        return method;
    }

    public Supplier<Schedule> getSchedule() {
        return schedule;
    }

    public RequestContext getContext() {
        return context;
    }

    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }
}
