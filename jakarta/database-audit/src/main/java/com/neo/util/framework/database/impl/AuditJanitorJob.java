package com.neo.util.framework.database.impl;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.janitor.JanitorJob;
import com.neo.util.framework.database.api.PersistenceContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;

//TODO REQUIRES TESTING
@ApplicationScoped
public class AuditJanitorJob implements JanitorJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditJanitorJob.class);

    private static final String CONFIG_AUDIT_RETENTION = "entity.audit.retention";

    @Inject
    protected PersistenceContextProvider pcp;

    @Inject
    protected ConfigService configService;


    @Override
    @Transactional
    public void execute(LocalDate now) {
        LOGGER.info("Starting cleanup for EntityAuditTrail...");

        LocalDate retentionDate = now.minus(getConfiguredRetention());

        LOGGER.debug("Deleting everything older than [{}]", retentionDate);

        String query = """
                DELETE FROM EntityAuditTrail audit
                WHERE audit.createdOn < :retentionDate
                """;

        int deletedEntries = pcp.getEm().createQuery(query).setParameter("retentionDate", retentionDate).executeUpdate();

        LOGGER.info("Finished cleanup for EntityAuditTrail [{}] entries deleted", deletedEntries);
    }

    public Period getConfiguredRetention() {
        return configService.get(CONFIG_AUDIT_RETENTION).asInt().map(Period::ofDays).orElse(Period.ofMonths(1));
    }
}
