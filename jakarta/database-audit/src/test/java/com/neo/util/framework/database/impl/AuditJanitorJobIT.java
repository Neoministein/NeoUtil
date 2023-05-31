package com.neo.util.framework.database.impl;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.impl.config.BasicConfigService;
import com.neo.util.framework.impl.config.BasicConfigValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;

class AuditJanitorJobIT extends AbstractIntegrationTest<AuditJanitorJob> {

    @Override
    protected Class<AuditJanitorJob> getSubjectClass() {
        return AuditJanitorJob.class;
    }

    @Test
    void auditCreationTest() {
        ConfigService configService = getInstance(BasicConfigService.class);
        AuditTrailRepository auditTrailRepository = getInstance(AuditTrailRepository.class);

        configService.save(new BasicConfigValue<>(AuditJanitorJob.CONFIG_AUDIT_RETENTION, 7));

        auditTrailRepository.create(new EntityAuditTrail("", "", ""));
        auditTrailRepository.create(new EntityAuditTrail("", "", ""));

        subject.execute(LocalDate.now().minus(Period.ofDays(7)));
        Assertions.assertEquals(2 ,auditTrailRepository.count());
        subject.execute(LocalDate.now().minus(Period.ofDays(8)));
    }
}