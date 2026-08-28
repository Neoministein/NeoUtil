package com.neo.util.framework.jobrunr.scheduler.impl;

import com.neo.util.common.impl.exception.InternalRuntimeException;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.scheduler.SchedulerConfig;
import com.neo.util.framework.api.scheduler.SchedulerService;
import com.neo.util.framework.impl.ReflectionService;
import com.neo.util.framework.impl.config.ConfigServiceImpl;
import com.neo.util.framework.impl.config.store.InMemoryConfigStore;
import com.neo.util.framework.impl.janitor.JanitorServiceImpl;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.impl.request.RequestDetailsProducer;
import com.neo.util.framework.impl.request.Slf4jRequestAuditProvider;
import com.neo.util.framework.impl.security.BasicInstanceIdentification;
import com.neo.util.framework.jobrunr.impl.JobRunnerConfigurator;
import com.neo.util.framework.jobrunr.impl.JobRunnerInMemoryStorageProvider;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJunit5Extension.class)
class SchedulerServiceIT {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            JobRunnerConfigurator.class,
            JobRunnerInMemoryStorageProvider.class,
            JobRunnerSchedulerConfigHolder.class,
            ConfigServiceImpl.class,
            InMemoryConfigStore.class,
            BasicInstanceIdentification.class,
            ReflectionService.class,
            IndexReflectionProvider.class,
            RequestContextExecutor.class,
            RequestDetailsProducer.class,
            Slf4jRequestAuditProvider.class,
            JobRunnerSchedulerService.class,
            TestSchedulers.class,
            JanitorServiceImpl.class
    ).build();

    protected SchedulerService schedulerService;

    protected TestSchedulers testSchedulers;

    @BeforeEach
    void before() {
        weld.select(ConfigService.class).get().save(1, JobRunnerConfigurator.CONFIG_PREFIX + JobRunnerConfigurator.CONFIG_POLL_INTERVAL);

        JobRunnerConfigurator jobRunnerConfigurator = weld.select(JobRunnerConfigurator.class).get();
        jobRunnerConfigurator.preReadyEvent(new ApplicationPreReadyEvent());

        testSchedulers = weld.select(TestSchedulers.class).get();
    }

    @Test
    void unknownSchedulers() {
        setupSchedulers();

        Assertions.assertThrows(InternalRuntimeException.class, () -> schedulerService.execute(""));
        Assertions.assertThrows(InternalRuntimeException.class, () -> schedulerService.start(""));
    }

    @Test
    void executeScheduler() {
        setupSchedulers();

        schedulerService.execute("cron");
        Assertions.assertEquals(1, testSchedulers.getCronExecutionCount());
    }

    @Test
    void runningScheduler() {
        setupSchedulers();

        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(2, testSchedulers.getCronExecutionCount());
        });
    }

    @Test
    void stopScheduler() {
        setupSchedulers();
        schedulerService.stop("cron");
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(0, testSchedulers.getCronExecutionCount());
        });
    }

    @Test
    void disableScheduler() {
        weld.select(ConfigService.class).get().save(false, "scheduler.cron.enabled");

        setupSchedulers();
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(0, testSchedulers.getCronExecutionCount());
        });
    }

    @Test
    void invalidChronInConfig() {
        setupSchedulers();
        weld.select(ConfigService.class).get().save("1", "scheduler.cron.cron");

        Assertions.assertThrows(InternalRuntimeException.class, () -> schedulerService.reload());
    }

    @Test
    void updateToInvalidCron() {
        setupSchedulers();
        SchedulerConfig schedulerConfig = schedulerService.requestSchedulerConfig("cron");
        schedulerConfig.setCronValue("1");
        Assertions.assertThrows(ValidationException.class, () -> schedulerService.updateConfig(schedulerConfig));
    }

    @Test
    void changeType() {
        setupSchedulers();
        weld.select(ConfigService.class).get().save("0/5 * * * * *", "scheduler.interval.cron");

        Assertions.assertDoesNotThrow(() -> schedulerService.reload());
    }

    @Test
    void annotatedInterface() {
        setupSchedulers();
        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertEquals(1, testSchedulers.getInterfaceExecutionCount());
        });
    }

    protected void setupSchedulers() {
        JobRunnerSchedulerService jobRunnerSchedulerService = weld.select(JobRunnerSchedulerService.class).get();
        jobRunnerSchedulerService.applicationReadyEvent(new ApplicationReadyEvent());
        schedulerService = jobRunnerSchedulerService;
    }
}
