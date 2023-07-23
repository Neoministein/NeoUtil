package com.neo.util.framework.jobrunr.scheduler.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface ScheduleAnnotationParser<T extends Annotation> {

    SchedulerConfig parseToBasicConfig(Method method);

    Class<T> getType();
}
