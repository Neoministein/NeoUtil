package com.neo.util.framework.impl.janitor;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.api.janitor.JanitorConfig;
import com.neo.util.framework.api.janitor.JanitorJob;
import com.neo.util.framework.api.janitor.JanitorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class JanitorServiceImpl implements JanitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JanitorServiceImpl.class);

    private static final String CONFIG_PREFIX = "janitor.";

    private static final String CONFIG_DISABLED_POSTFIX = ".disabled";

    public static final ExceptionDetails EX_DUPLICATED_JANITOR_JOB = new ExceptionDetails(
            "janitor/duplicated-janitor-configured", "Duplicated Janitor configured [{0}], [{1}]", true);

    protected static final ExceptionDetails EX_NON_EXISTENT_JANITOR_JOB = new ExceptionDetails(
            "janitor/non-existent-janitor", "The Janitor [{0}] does not exist", true
    );

    protected final ConfigService configService;

    protected final Map<String, JanitorConfig> janitorJobMap = new ConcurrentHashMap<>();

    protected void postReadyEvent(@Observes ApplicationPostReadyEvent applicationPostReadyEvent) {
        LOGGER.info("Post Ready Event received");
    }

    /**
     * Initializes the mapping to the {@link JanitorJob}.
     */
    @Inject
    public JanitorServiceImpl(Instance<JanitorJob> janitorJobInstance, ConfigService configService) {
        LOGGER.info("Registering JanitorJobs...");
        this.configService = configService;

        for (JanitorJob janitorJob: janitorJobInstance) {
            boolean enabled = configService.get(CONFIG_PREFIX + janitorJob.getJanitorId() + CONFIG_DISABLED_POSTFIX).asBoolean().orElse(false);

            JanitorConfig janitorConfig = new JanitorConfig(janitorJob.getJanitorId(), enabled, janitorJob);

            JanitorConfig existingJanitor =  janitorJobMap.putIfAbsent(janitorJob.getJanitorId(), janitorConfig);

            if (existingJanitor != null) {
                throw new ConfigurationException(EX_DUPLICATED_JANITOR_JOB,
                        janitorJob.getClass().getName(), existingJanitor.getClass().getName());
            }

            LOGGER.debug("Registered JanitorJob [{}]", janitorJob.getJanitorId());
        }

        LOGGER.info("Registered [{}] JanitorJobs {}", janitorJobMap.size(), janitorJobMap.keySet());
    }

    @Override
    public JanitorConfig getJanitorConfig(String janitorId) throws NoContentFoundException {
        return Optional.ofNullable(janitorJobMap.get(janitorId)).orElseThrow(() -> new NoContentFoundException(EX_NON_EXISTENT_JANITOR_JOB, janitorId));
    }

    @Override
    public void execute(String janitorId) {
        execute(getJanitorConfig(janitorId), LocalDate.now());
    }

    protected void execute(JanitorConfig janitorConfig, LocalDate now) {
        try {
            LOGGER.info("Executing Janitor Job [{}]", janitorConfig.getId());
            janitorConfig.getJanitorJob().execute(now);
            janitorConfig.setLastExecutionFailed(false);
        } catch (Exception ex) {
            LOGGER.info("Unexpected error occurred while processing Janitor Job [{}], action won't be retried.", janitorConfig.getId(), ex);
            janitorConfig.setLastExecutionFailed(true);
        }
        janitorConfig.setLastExecution(Instant.now());
    }

    @Override
    public void executeAll() {
        LocalDate now = LocalDate.now();

        LOGGER.info("Executing all Janitor Jobs...");
        for (JanitorConfig janitorConfig: janitorJobMap.values()) {
            if (janitorConfig.isEnabled()) {
                execute(janitorConfig, now);
            } else {
                LOGGER.info("Skipping disabled Janitor Job [{}]", janitorConfig.getId());
            }
        }
        LOGGER.info("Finished executing all Janitor Jobs");
    }

    @Override
    public void enable(String janitorId) {
        LOGGER.info("Enabling Janitor Job [{}]", janitorId);
        getJanitorConfig(janitorId).setEnabled(true);
    }

    @Override
    public void disable(String janitorId) {
        LOGGER.info("Disabling Janitor Job [{}]", janitorId);
        getJanitorConfig(janitorId).setEnabled(false);
    }

    @Override
    public Set<String> getJanitorIds() {
        return janitorJobMap.keySet();
    }
}
