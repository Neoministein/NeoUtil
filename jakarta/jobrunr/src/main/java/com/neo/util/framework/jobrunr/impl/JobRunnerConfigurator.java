package com.neo.util.framework.jobrunr.impl;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.event.ApplicationShutdownEvent;
import com.neo.util.framework.jobrunr.api.JobRunrStorageProvider;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.inject.Inject;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.JobActivator;
import org.jobrunr.utils.reflection.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JobRunnerConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerConfigurator.class);

    public static final String CONFIG_PREFIX = "jobrunr.";
    public static final String CONFIG_ENABLED = ".enabled";
    public static final String CONFIG_BACKGROUND_WORKER = "backgroundWorkers";
    public static final String CONFIG_DASHBOARD = "backgroundWorkers";
    public static final String CONFIG_POLL_INTERVAL = "pollInterval";

    private static final int DEFAULT_DASHBOARD_PORT = 8050;
    private static final int DEFAULT_WORKERS = 1;
    private static final int DEFAULT_POLL_INTERVAL = 5;

    @Inject
    protected ConfigService configService;

    @Inject
    protected JobRunrStorageProvider jobRunrStorageProvider;

    @Inject
    protected Instance<Object> instance;

    @PostConstruct
    public void init() {
        LOGGER.info("Loading JobRunr configuration");

        boolean backGroundWorkerEnabled = configService.get(CONFIG_PREFIX + CONFIG_BACKGROUND_WORKER + CONFIG_ENABLED).asBoolean().orElse(true);
        int backGroundWorkers = configService.get(CONFIG_PREFIX + CONFIG_BACKGROUND_WORKER).asInt().orElse(DEFAULT_WORKERS);
        int pollInterval = configService.get(CONFIG_PREFIX + CONFIG_POLL_INTERVAL).asInt().orElse(DEFAULT_POLL_INTERVAL);
        LOGGER.info("JobRunrConfiguration, BackgroundJobServer: [{}], workers: [{}] pollIntervalInSeconds: [{}]", backGroundWorkerEnabled, backGroundWorkers, pollInterval);

        boolean dashboardEnabled = configService.get(CONFIG_PREFIX + CONFIG_DASHBOARD + CONFIG_ENABLED).asBoolean().orElse(false);
        int dashboardPort = configService.get(CONFIG_PREFIX + CONFIG_DASHBOARD).asInt().orElse(DEFAULT_DASHBOARD_PORT);
        LOGGER.info("JobRunrConfiguration, Dashboard: [{}], ports: [{}]", dashboardEnabled, dashboardPort);

        JobActivator jobActivator = new JobActivator() {
            @Override
            public <T> T activateJob(Class<T> clazz) {
                T type;
                try {
                    type = instance.select(clazz).get();
                } catch (UnsatisfiedResolutionException ex) {
                    LOGGER.warn("Class [{}] is not a CDI Bean, falling back to reflection construction", clazz.getSimpleName());
                    type = ReflectionUtils.newInstance(clazz);
                }
                return type;
            }
        };

        BackgroundJobServerConfiguration backgroundJobServerConfiguration = BackgroundJobServerConfiguration
                .usingStandardBackgroundJobServerConfiguration()
                .andPollIntervalInSeconds(pollInterval)
                .andWorkerCount(backGroundWorkers);

        JobRunr.configure()
                .useStorageProvider(jobRunrStorageProvider.get())
                .useJobActivator(jobActivator)
                .useBackgroundJobServerIf(backGroundWorkerEnabled, backgroundJobServerConfiguration)
                .useDashboardIf(dashboardEnabled, dashboardPort)
                .initialize();
    }

    public void preReadyEvent(@Observes ApplicationPreReadyEvent applicationPreReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent processed");
    }

    public void shutDownEvent(@Observes ApplicationShutdownEvent applicationShutdownEvent) {
        JobRunr.destroy();
        LOGGER.debug("ApplicationShutdownEvent processed");
    }
}
