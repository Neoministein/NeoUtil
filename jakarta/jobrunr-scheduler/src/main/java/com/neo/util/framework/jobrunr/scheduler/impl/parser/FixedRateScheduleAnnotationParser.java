package com.neo.util.framework.jobrunr.scheduler.impl.parser;

import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import com.neo.util.framework.jobrunr.scheduler.api.JobRunnerSchedulerConfig;
import com.neo.util.framework.jobrunr.scheduler.api.ScheduleAnnotationParser;
import jakarta.enterprise.context.ApplicationScoped;
import org.jobrunr.scheduling.interval.Interval;

import java.lang.reflect.Method;
import java.time.Duration;

@ApplicationScoped
public class FixedRateScheduleAnnotationParser implements ScheduleAnnotationParser<FixedRateSchedule> {

    @Override
    public JobRunnerSchedulerConfig parseToBasicConfig(Method method) {
        FixedRateSchedule fixedRateSchedule = method.getAnnotation(FixedRateSchedule.class);
        return new JobRunnerSchedulerConfig(fixedRateSchedule.value(), method, () -> new Interval(Duration.ofSeconds(fixedRateSchedule.timeUnit().toSeconds(fixedRateSchedule.delay()))));
    }

    @Override
    public Class<FixedRateSchedule> getType() {
        return FixedRateSchedule.class;
    }


}


