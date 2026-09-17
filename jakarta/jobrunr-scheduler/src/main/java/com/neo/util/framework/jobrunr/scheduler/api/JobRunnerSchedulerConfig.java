package com.neo.util.framework.jobrunr.scheduler.api;

import com.neo.util.api.request.RequestContext;
import com.neo.util.api.scheduler.SchedulerConfig;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JobRunnerSchedulerConfig {

    public static final ExceptionDetails EX_METHOD_NOT_ACCESSIBLE = new ExceptionDetails(
            "scheduler/method-not-accessible", "Scheduler is not accessible [{0}.{1}].");

    protected final RequestContext context;
    protected final SchedulerConfig config;
    protected final Method method;
    protected final Object beanInstance;;

    public JobRunnerSchedulerConfig(SchedulerConfig config, Method method, Object beanInstance) {
        this.context = new SchedulerRequestDetails.Context(config.getId());
        this.config = config;
        this.method = method;
        this.beanInstance = beanInstance;

        if (Modifier.isPrivate(method.getModifiers()) || !method.trySetAccessible()) {
            throw new ConfigurationException(EX_METHOD_NOT_ACCESSIBLE, method.getDeclaringClass().getName(),
                    method.getName());
        }
    }

    public RequestContext getContext() {
        return context;
    }

    public SchedulerConfig getConfig() {
        return config;
    }

    public Method getMethod() {
        return method;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }
}