package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.api.scheduler.SchedulerConfig;
import com.neo.util.framework.api.scheduler.SchedulerService;
import com.neo.util.framework.impl.ReflectionService;
import com.neo.util.framework.jobrunr.scheduler.api.JobRunnerSchedulerConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jobrunr.scheduling.cron.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

@ApplicationScoped
public class JobRunnerSchedulerConfigHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerSchedulerConfigHolder.class);

    private final Map<String, JobRunnerSchedulerConfig> schedulers;

    protected ConfigService configService;

    /**
     * Initializes the mapping for the {@link JobRunnerSchedulerConfig}.
     */
    @Inject
    public JobRunnerSchedulerConfigHolder(Instance<Object> instance, ReflectionService reflectionService,
            ConfigService configService) {
        this.configService = configService;

        LOGGER.info("Registering Schedulers...");

        Map<String, JobRunnerSchedulerConfig> configMap = new HashMap<>();
        Set<AnnotatedElement> schedulerElements = reflectionService.getAnnotatedElement(CronSchedule.class);
        for (AnnotatedElement schedulerElement: schedulerElements) {
            Method schedulerMethod = (Method) schedulerElement;
            JobRunnerSchedulerConfig config = createConfigFromMethod(schedulerMethod, instance);
            String configId = config.getConfig().getId();
            if (configMap.containsKey(configId)) {
                throw new ConfigurationException(SchedulerService.EX_DUPLICATED_SCHEDULER, configId);
            }

            configMap.put(configId, config);

            reloadFromConfig(config.getConfig());
            updateConfig(config.getConfig());
            LOGGER.debug("Registered Scheduler [{}]", configId);
        }
        schedulers = Collections.unmodifiableMap(configMap);
        LOGGER.info("Registered [{}] Schedules {}", schedulers.size(), schedulers.keySet());
    }

    public JobRunnerSchedulerConfig requestSchedulerConfig(String id) {
        return Optional.ofNullable(schedulers.get(id))
                .orElseThrow(() -> new NoContentFoundException(SchedulerService.EX_UNKNOWN_SCHEDULER_ID, id));
    }

    public Map<String, JobRunnerSchedulerConfig> getSchedulers() {
        return schedulers;
    }

    public void reloadFromConfig() {
        for (JobRunnerSchedulerConfig schedulerConfig: schedulers.values()) {
            reloadFromConfig(schedulerConfig.getConfig());
        }
    }

    private void reloadFromConfig(SchedulerConfig schedulerConfig) {
        String configPrefix = "scheduler." + schedulerConfig.getId();
        configService.getAsBoolean(configPrefix, "enabled").ifPresent(schedulerConfig::setEnabled);
        configService.getAsString(configPrefix, "cron").ifPresent(schedulerConfig::setCronValue);
        configService.getAsLong(configPrefix, "lastExecution").map(Instant::ofEpochMilli)
                .ifPresent(schedulerConfig::setLastExecution);
        configService.getAsBoolean(configPrefix, "lastExecutionFailed")
                .ifPresent(schedulerConfig::setLastExecutionFailed);
    }

    public void updateConfig(SchedulerConfig schedulerConfig) {
        try {
            new CronExpression(schedulerConfig.getCronValue());
        } catch (RuntimeException ex) {
            throw new ValidationException(SchedulerConfig.INVALID_CRON_EXPRESSION, schedulerConfig.getId(),
                    schedulerConfig.getCronValue());
        }

        String configPrefix = "scheduler." + schedulerConfig.getId();
        configService.save(schedulerConfig.isEnabled(), configPrefix, "enabled");
        configService.save(schedulerConfig.getCronValue(), configPrefix, "cron");
        configService.save(schedulerConfig.getLastExecution().toEpochMilli(), configPrefix, "lastExecution");
        configService.save(schedulerConfig.getLastExecutionFailed(), configPrefix, "lastExecutionFailed");
    }

    protected JobRunnerSchedulerConfig createConfigFromMethod(Method method, Instance<Object> instance) {
        CronSchedule cronSchedule = method.getAnnotation(CronSchedule.class);
        SchedulerConfig config = new SchedulerConfig(cronSchedule.value(), true, cronSchedule.cron());
        return new JobRunnerSchedulerConfig(config, method, getBeanInstance(method, instance));
    }

    protected Object getBeanInstance(Method method, Instance<Object> instance) {
        return instance.select(method.getDeclaringClass()).get();
    }
}