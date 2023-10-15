package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.common.impl.MathUtils;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.scheduler.SchedulerService;
import com.neo.util.framework.impl.ReflectionService;
import com.neo.util.framework.impl.config.BasicConfigService;
import com.neo.util.framework.impl.config.BasicConfigValue;
import com.neo.util.framework.impl.janitor.JanitorServiceImpl;
import com.neo.util.framework.impl.request.DummyRequestAuditProvider;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.impl.request.RequestDetailsProducer;
import com.neo.util.framework.impl.security.BasicInstanceIdentification;
import com.neo.util.framework.jobrunr.impl.JobRunnerConfigurator;
import com.neo.util.framework.jobrunr.impl.JobRunnerInMemoryStorageProvider;
import com.neo.util.framework.jobrunr.scheduler.impl.parser.CronScheduleAnnotationParser;
import com.neo.util.framework.jobrunr.scheduler.impl.parser.FixedRateScheduleAnnotationParser;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ExtendWith(WeldJunit5Extension.class)
class SchedulerServiceIT {

    private static final BasicConfigValue<Integer> POOL_INTERVAL = new BasicConfigValue<>(JobRunnerConfigurator.CONFIG_PREFIX + JobRunnerConfigurator.CONFIG_POLL_INTERVAL, 1);

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            JobRunnerConfigurator.class,
            JobRunnerInMemoryStorageProvider.class,
            BasicConfigService.class,
            BasicInstanceIdentification.class,
            ReflectionService.class,
            IndexReflectionProvider.class,
            RequestContextExecutor.class,
            RequestDetailsProducer.class,
            DummyRequestAuditProvider.class,
            JobRunnerSchedulerService.class,
            CronScheduleAnnotationParser.class,
            FixedRateScheduleAnnotationParser.class,
            TestSchedulers.class,
            JanitorServiceImpl.class
    ).build();

    protected SchedulerService schedulerService;

    protected TestSchedulers testSchedulers;

    @BeforeEach
    void before() {
        weld.select(BasicConfigService.class).get().save(POOL_INTERVAL);

        JobRunnerConfigurator jobRunnerConfigurator = weld.select(JobRunnerConfigurator.class).get();
        jobRunnerConfigurator.preReadyEvent(new ApplicationPreReadyEvent());

        testSchedulers = weld.select(TestSchedulers.class).get();
    }

    @Test
    void unknownSchedulers() {
        setupSchedulers();

        Assertions.assertThrows(CommonRuntimeException.class, () -> schedulerService.executeScheduler(""));
        Assertions.assertThrows(CommonRuntimeException.class, () -> schedulerService.startScheduler(""));
    }

    @Test
    void executeScheduler() {
        setupSchedulers();

        schedulerService.executeScheduler("interval");
        Assertions.assertEquals(1, testSchedulers.getIntervalExecutionCount());
    }

    @Test
    void runningScheduler() {
        setupSchedulers();

        Instant start = Instant.now();
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(1, testSchedulers.getIntervalExecutionCount());
            Assertions.assertEquals(2, testSchedulers.getCronExecutionCount());
            return true;
        });

        int basicDelay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(basicDelay,9000, 11000), "The delay " + basicDelay);
        System.out.println(basicDelay);
    }

    @Test
    void stopScheduler() {
        setupSchedulers();
        schedulerService.stopScheduler("cron");
        Instant start = Instant.now();
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(1, testSchedulers.getIntervalExecutionCount());
            Assertions.assertEquals(0, testSchedulers.getCronExecutionCount());
            return true;
        });

        int basicDelay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(basicDelay,9000, 11000), "The delay " + basicDelay);
        System.out.println(basicDelay);
    }

    @Test
    void disableScheduler() {
        BasicConfigValue<Boolean> disable = new BasicConfigValue<>("scheduler.cron.enabled", false);
        weld.select(BasicConfigService.class).get().save(disable);

        setupSchedulers();
        Instant start = Instant.now();
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(1, testSchedulers.getIntervalExecutionCount());
            Assertions.assertEquals(0, testSchedulers.getCronExecutionCount());
            return true;
        });

        int basicDelay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(basicDelay,9000, 11000), "The delay " + basicDelay);
    }

    @Test
    void invalidChron() {
        setupSchedulers();
        BasicConfigValue<String> invalidChron = new BasicConfigValue<>("scheduler.cron.cron", "1");
        weld.select(BasicConfigService.class).get().save(invalidChron);

        Assertions.assertThrows(ValidationException.class, () -> schedulerService.reload());
    }

    @Test
    void invalidInterval() {
        setupSchedulers();
        BasicConfigValue<Long> invalidInterval = new BasicConfigValue<>("scheduler.interval.delay", -1L);
        BasicConfigValue<String> timeUnit = new BasicConfigValue<>("scheduler.interval.time-unit", "SECONDS");
        weld.select(BasicConfigService.class).get().save(invalidInterval);
        weld.select(BasicConfigService.class).get().save(timeUnit);

        Assertions.assertThrows(ValidationException.class, () -> schedulerService.reload());
    }

    @Test
    void changeType() {
        setupSchedulers();
        BasicConfigValue<String> newConfig = new BasicConfigValue<>("scheduler.interval.cron", "0/5 * * * * *");
        weld.select(BasicConfigService.class).get().save(newConfig);

        Assertions.assertDoesNotThrow(() -> schedulerService.reload());
    }

    @Test
    void annotatedInterface() {
        setupSchedulers();
        Instant start = Instant.now();
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(1, testSchedulers.getInterfaceExecutionCount());
            return true;
        });

        int basicDelay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(basicDelay,4000, 6000), "The delay " + basicDelay);
    }

    protected void setupSchedulers() {
        JobRunnerSchedulerService jobRunnerSchedulerService = weld.select(JobRunnerSchedulerService.class).get();
        jobRunnerSchedulerService.applicationReadyEvent(new ApplicationPostReadyEvent());
        schedulerService = jobRunnerSchedulerService;
    }
}
