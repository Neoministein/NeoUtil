package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.common.impl.exception.InternalRuntimeException;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.scheduler.SchedulerConfig;
import com.neo.util.framework.api.scheduler.SchedulerService;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;
import com.neo.util.framework.jobrunr.scheduler.api.JobRunnerSchedulerConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.RecurringJobBuilder;
import org.jobrunr.scheduling.ScheduleException;
import org.jobrunr.scheduling.cron.InvalidCronExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JobRunnerSchedulerService implements SchedulerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerSchedulerService.class);

    protected InstanceIdentification instanceIdentification;
    protected JobRunnerSchedulerConfigHolder configHolder;
    protected RequestContextExecutor requestContextExecutor;

    public void applicationReadyEvent(@Observes ApplicationReadyEvent applicationPostReadyEvent) {
        LOGGER.debug("ApplicationPostReadyEvent processed");
    }

    /**
     * Initializes the mapping for the {@link JobRunnerSchedulerConfig}.
     */
    @Inject
    public JobRunnerSchedulerService(JobRunnerSchedulerConfigHolder configHolder,
                                     RequestContextExecutor requestContextExecutor, InstanceIdentification instanceIdentification) {
        this.configHolder = configHolder;
        this.requestContextExecutor = requestContextExecutor;
        this.instanceIdentification = instanceIdentification;

        startAllSchedulers();
    }

    public SchedulerConfig requestSchedulerConfig(String id) {
        return configHolder.requestSchedulerConfig(id).getConfig();
    }

    @Override
    public void reload() {
        configHolder.reloadFromConfig();

        configHolder.getSchedulers().keySet().forEach(this::stopJob);
        startAllSchedulers();
    }

    @Override
    public Set<String> getSchedulerIds() {
        return configHolder.getSchedulers().keySet();
    }

    @Override
    public void updateConfig(SchedulerConfig newConfig) {
        SchedulerConfig config = requestSchedulerConfig(newConfig.getId());
        config.setEnabled(newConfig.isEnabled());
        config.setCronValue(newConfig.getCronValue());

        configHolder.updateConfig(config);
        LOGGER.info("Updated {}", config);
        stop(config.getId());
        start(config.getId());
    }

    @Override
    public void start(String id) {
        JobRunnerSchedulerConfig schedulerConfig = configHolder.requestSchedulerConfig(id);
        schedulerConfig.getConfig().setEnabled(true);
        configHolder.updateConfig(schedulerConfig.getConfig());

        startScheduler(schedulerConfig);
    }

    protected void startAllSchedulers() {
        LOGGER.info("Starting schedulers...");
        for (JobRunnerSchedulerConfig schedulerConfig: configHolder.getSchedulers().values()) {

            startScheduler(schedulerConfig);
        }
        LOGGER.info("Finished starting schedulers");
    }

    protected void startScheduler(JobRunnerSchedulerConfig schedulerConfig) {
        LOGGER.debug("Starting scheduler [{}] [{}]", schedulerConfig.getConfig().getId(),
                schedulerConfig.getConfig().isEnabled());
        try {
            if (schedulerConfig.getConfig().isEnabled()) {
                JobLambda action = () -> execute(schedulerConfig.getConfig().getId());

                RecurringJobBuilder builder = RecurringJobBuilder.aRecurringJob()
                        .withId(schedulerConfig.getConfig().getId()).withJobLambda(action)
                        .withCron(schedulerConfig.getConfig().getCronValue());

                BackgroundJob.createRecurrently(builder);
            }
        } catch (InvalidCronExpressionException | IllegalArgumentException | ScheduleException ex) {
            throw new InternalRuntimeException(EX_INVALID_CONFIG_EXPRESSION, ex.getMessage());
        }
    }

    @Override
    public void execute(String id) {
        JobRunnerSchedulerConfig schedulerConfig = configHolder.requestSchedulerConfig(id);
        SchedulerConfig config = schedulerConfig.getConfig();

        CheckedRunnable<?> action = () -> {
            boolean failed = true;
            try {
                LOGGER.info("Executing scheduler [{}]", id);
                schedulerConfig.getMethod().invoke(schedulerConfig.getBeanInstance());
                failed = false;
            } finally {
                config.setLastExecutionFailed(failed);
                config.setLastExecution(Instant.now());
                configHolder.updateConfig(config);
            }
        };

        try {
            requestContextExecutor.executeChecked(new SchedulerRequestDetails(instanceIdentification.getInstanceId(), config.getContext()), action);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Unable to invoke [{}.{}] [{}]", schedulerConfig.getMethod().getDeclaringClass().getName(),
                    schedulerConfig.getMethod().getName(), ex.getMessage());
        } catch (Exception ex) {
            if (ex instanceof IllegalAccessException) {
                LOGGER.error("Unable to invoke [{}.{}] [{}]", schedulerConfig.getMethod().getDeclaringClass().getName(),
                        schedulerConfig.getMethod().getName(), ex.getMessage());
            } else if (ex instanceof InvocationTargetException) {
                LOGGER.error("The scheduler [{}] threw an exception", config.getId(), ex.getCause());
            } else {
                LOGGER.error("An unexpected exception while executing a scheduler.", ex);
            }
        }
    }

    @Override
    public void stop(String id) {
        SchedulerConfig schedulerConfig = requestSchedulerConfig(id);
        schedulerConfig.setEnabled(false);
        configHolder.updateConfig(schedulerConfig);

        stopJob(id);
    }

    protected void stopJob(String id) {
        BackgroundJob.deleteRecurringJob(id);
        LOGGER.info("Stopping scheduler [{}]", id);
    }

}