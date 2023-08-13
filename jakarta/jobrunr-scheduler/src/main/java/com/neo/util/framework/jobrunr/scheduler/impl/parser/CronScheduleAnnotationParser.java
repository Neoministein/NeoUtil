package com.neo.util.framework.jobrunr.scheduler.impl.parser;

import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.jobrunr.scheduler.api.JobRunnerSchedulerConfig;
import com.neo.util.framework.jobrunr.scheduler.api.ScheduleAnnotationParser;
import jakarta.enterprise.context.ApplicationScoped;
import org.jobrunr.scheduling.cron.CronExpression;

import java.lang.reflect.Method;

@ApplicationScoped
public class CronScheduleAnnotationParser implements ScheduleAnnotationParser<CronSchedule> {

    @Override
    public JobRunnerSchedulerConfig parseToBasicConfig(Method method) {
        CronSchedule cronSchedule = method.getAnnotation(CronSchedule.class);
        return new JobRunnerSchedulerConfig(cronSchedule.value(), method, () -> CronExpression.create(cronSchedule.cron()));
    }

    @Override
    public Class<CronSchedule> getType() {
        return CronSchedule.class;
    }
}
