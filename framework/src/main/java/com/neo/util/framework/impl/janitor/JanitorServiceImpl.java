package com.neo.util.framework.impl.janitor;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import com.neo.util.framework.api.event.ApplicationPostReadyEvent;
import com.neo.util.framework.api.janitor.JanitorJob;
import com.neo.util.framework.api.janitor.JanitorService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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

    @Inject
    protected ConfigService configService;

    protected Map<String, JanitorJob> janitorJobMap;

    protected void postReadyEvent(@Observes ApplicationPostReadyEvent applicationPostReadyEvent) {
        LOGGER.info("Post Ready Event received");
    }

    /**
     * Initializes the mapping to the {@link JanitorJob}.
     * This is done only once at startup as no new Janitors should be added at runtime.
     */
    @Inject
    public void init(Instance<JanitorJob> janitorJobInstance) {
        LOGGER.info("Registering JanitorJobs...");
        for (JanitorJob janitorJob: janitorJobInstance) {
            JanitorJob existingJanitor =  janitorJobMap.putIfAbsent(janitorJob.getJanitorId(), janitorJob);

            if (existingJanitor != null) {
                throw new ConfigurationException(EX_DUPLICATED_JANITOR_JOB,
                        janitorJob.getClass().getName(), existingJanitor.getClass().getName());
            }

            LOGGER.debug("Registered JanitorJob [{}]", janitorJob.getJanitorId());
        }
        LOGGER.info("Finished registering JanitorJobs");
    }

    @Override
    public Collection<String> getJanitorNames() {
        return janitorJobMap.keySet();
    }

    @Override
    public void execute(String janitorId) {
        Optional<JanitorJob> optionalJanitorJob = Optional.ofNullable(janitorJobMap.get(janitorId));

        JanitorJob janitorJob = optionalJanitorJob.orElseThrow(() ->  new ConfigurationException(EX_NON_EXISTENT_JANITOR_JOB));

        LOGGER.info("Executing Janitor Job [{}]", janitorId);
        janitorJob.execute(LocalDate.now());
    }

    @Override
    public void executeAll() {
        LocalDate localDate = LocalDate.now();

        LOGGER.info("Executing all Janitor Jobs...");
        for (Map.Entry<String, JanitorJob> janitorJobEntry: janitorJobMap.entrySet()) {
            if (!isJanitorDisabled(janitorJobEntry.getKey())) {
                LOGGER.info("Executing Janitor Job [{}]", janitorJobEntry.getKey());
                janitorJobEntry.getValue().execute(localDate);
            } else {
                LOGGER.info("Skipping disabled Janitor Job [{}]", janitorJobEntry.getKey());
            }
        }
        LOGGER.info("Finished executing all Janitor Jobs");
    }

    @Override
    public void disableJanitor(String janitorId) {
        LOGGER.info("Disabling Janitor Job [{}]", janitorId);
        if (!janitorJobMap.containsKey(janitorId)) {
            throw new ConfigurationException(EX_NON_EXISTENT_JANITOR_JOB);
        }

        ConfigValue<Boolean> configValue = configService.newConfig(CONFIG_PREFIX + janitorId + CONFIG_DISABLED_POSTFIX, true);
        configService.save(configValue);
    }

    protected boolean isJanitorDisabled(String janitorId) {
        Config janitorConfig = configService.get(CONFIG_PREFIX + janitorId + CONFIG_DISABLED_POSTFIX);

        return janitorConfig.asBoolean().orElse(false);
    }
}
