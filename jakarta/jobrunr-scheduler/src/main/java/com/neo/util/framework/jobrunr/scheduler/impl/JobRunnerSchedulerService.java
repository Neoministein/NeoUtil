package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.common.api.func.CheckedRunnable;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.api.scheduler.SchedulerService;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.ReflectionService;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.impl.request.SchedulerRequestDetails;
import com.neo.util.framework.jobrunr.scheduler.api.ScheduleAnnotationParser;
import com.neo.util.framework.jobrunr.scheduler.api.SchedulerConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.RecurringJobBuilder;
import org.jobrunr.scheduling.Schedule;
import org.jobrunr.scheduling.cron.CronExpression;
import org.jobrunr.scheduling.cron.InvalidCronExpressionException;
import org.jobrunr.scheduling.interval.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class JobRunnerSchedulerService implements SchedulerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerSchedulerService.class.getName());

    private boolean inStartupPhase = true;
    private Map<String, SchedulerConfig> schedulers = new HashMap<>();

    @Inject
    protected ConfigService configService;

    @Inject
    protected InstanceIdentification identification;

    @Inject
    protected ReflectionService reflectionService;

    @Inject
    protected RequestContextExecutor requestContextExecutor;

    public void applicationReadyEvent(@Observes ApplicationPostReadyEvent applicationPostReadyEvent) {
        LOGGER.debug("ApplicationPostReadyEvent processed");
        inStartupPhase = false;
    }

    /**
     * Initializes the mapping for the {@link SchedulerConfig}.
     * This is done only once at startup as no new Scheduler can be added at runtime.
     */
    @Inject
    public void init(Instance<Object> instance, Instance<ScheduleAnnotationParser<?>> instance2) {
        Map<String, SchedulerConfig> newSchedulersMap = new HashMap<>();
        LOGGER.info("Registering Schedulers...");

        for (ScheduleAnnotationParser<?> parser: instance2) {
            LOGGER.trace("Found [{}] for Annotation [{}]", parser.getClass().getSimpleName(), parser.getType().getSimpleName());

            Set<AnnotatedElement> schedulerElements = reflectionService.getAnnotatedElement(parser.getType());
            for (AnnotatedElement schedulerElement : schedulerElements) {
                Method schedulerMethod = (Method) schedulerElement;
                SchedulerConfig config = parser.parseToBasicConfig(schedulerMethod);

                if (newSchedulersMap.containsKey(config.getId())) {
                    throw new ConfigurationException(EX_DUPLICATED_SCHEDULER, config.getId());
                }

                config.setBeanInstance(getBeanInstance(schedulerMethod, instance));

                if (Modifier.isPrivate(schedulerMethod.getModifiers()) || !schedulerMethod.trySetAccessible()) {
                    throw new ConfigurationException(EX_METHOD_NOT_ACCESSABLE,
                            schedulerMethod.getDeclaringClass().getName(),
                            schedulerMethod.getName());
                }

                newSchedulersMap.put(config.getId(), config);
                LOGGER.debug("Registered Scheduler [{}]", config.getId());
            }
        }

        LOGGER.info("Registered [{}] JanitorJobs {}", newSchedulersMap.size(), newSchedulersMap.keySet());
        schedulers = newSchedulersMap;
    }

    @Override
    @PostConstruct
    public void reload() {
        LOGGER.info("Starting schedulers...");
        for (SchedulerConfig schedulerConfig: schedulers.values()) {
            startScheduler(schedulerConfig, false);
        }
        LOGGER.info("Finished starting schedulers");
    }

    @Override
    public void startScheduler(String id) {
        LOGGER.info("Starting scheduler [{}]", id);
        SchedulerConfig schedulerConfig = Optional.ofNullable(schedulers.get(id)).
                orElseThrow(() -> new CommonRuntimeException(EX_INVALID_SCHEDULER_ID, id));

        startScheduler(schedulerConfig, true);
    }

    protected void startScheduler(SchedulerConfig schedulerConfig, boolean forceStart) {
        LOGGER.debug("Starting scheduler [{}], force [{}]", schedulerConfig.getId(), forceStart);
        try {
            Config config = configService.get("scheduler").get(schedulerConfig.getId());
            if (forceStart || config.get("enabled").asBoolean().orElse(true)) {
                JobLambda action = () -> executeScheduler(schedulerConfig.getId());

                RecurringJobBuilder builder = RecurringJobBuilder.aRecurringJob()
                        .withId(schedulerConfig.getId())
                        .withDetails(action)
                        .withSchedule(parseConfigToSchedule(config).orElseGet(schedulerConfig.getSchedule()));

                BackgroundJob.createRecurrently(builder);
            } else {
                LOGGER.debug("Not starting scheduler [{}] since it's not enabled", schedulerConfig.getId());
                stopScheduler(schedulerConfig.getId());
            }
        } catch (InvalidCronExpressionException | IllegalArgumentException ex) {
            throw new ValidationException(EX_INVALID_CONFIG_EXPRESSION, ex.getMessage());
        }
    }

    protected Optional<Schedule> parseConfigToSchedule(Config config) {
        ConfigValue<String> cron = config.get("cron").asString();
        if (cron.isPresent()) {
            return cron.map(CronExpression::create);
        } else {
            ConfigValue<Long> fixed = config.get("delay").asLong();
            Optional<TimeUnit> timeUnit = config.get("time-unit").asString().map(TimeUnit::valueOf);
            if (fixed.isPresent() && timeUnit.isPresent()) {
                return Optional.of(new Interval(Duration.ofSeconds(timeUnit.get().toSeconds(fixed.get()))));
            }
        }
        return Optional.empty();
    }

    @Override
    public void executeScheduler(String id) {
        SchedulerConfig schedulerConfig = Optional.ofNullable(schedulers.get(id)).
                orElseThrow(() -> new CommonRuntimeException(EX_INVALID_SCHEDULER_ID, id));

        CheckedRunnable<?> action = () -> {
            LOGGER.info("Executing scheduler [{}]", id);
            schedulerConfig.getMethod().invoke(schedulerConfig.getBeanInstance());
        };

        try {
            requestContextExecutor.executeChecked(new SchedulerRequestDetails(identification.getInstanceId(), schedulerConfig.getContext()), action);
        } catch (IllegalArgumentException  ex) {
            LOGGER.error("Unable to invoke [{}.{}] [{}]",
                    schedulerConfig.getMethod().getDeclaringClass().getName(),
                    schedulerConfig.getMethod().getName(), ex.getMessage());
        } catch (Exception ex) {
            if (ex instanceof IllegalAccessException || ex instanceof InvocationTargetException) {
                LOGGER.error("Unable to invoke [{}.{}] [{}]",
                        schedulerConfig.getMethod().getDeclaringClass().getName(),
                        schedulerConfig.getMethod().getName(), ex.getMessage());
            } else {
                LOGGER.error("An unexpected exception occurred ", ex);
            }
        }
    }

    @Override
    public void stopScheduler(String id) {
        BackgroundJob.delete(id);
        if (!inStartupPhase) {
            LOGGER.info("Stopping scheduler [{}]", id);
        }
    }

    protected Object getBeanInstance(Method method, Instance<Object> instance) {
        return instance.select(method.getDeclaringClass()).get();
    }
}