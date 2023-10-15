package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.enumeration.PersistenceOperation;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.persistence.entity.AuditParameter;
import com.neo.util.framework.api.persistence.entity.EntityAuditTrailProvider;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.persistence.EntityAuditTrail;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alternative
@Priority(PriorityConstants.APPLICATION)
@ApplicationScoped
public class DatabaseAuditTrailProvider implements EntityAuditTrailProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAuditTrailProvider.class);

    @Inject
    protected AuditTrailRepository auditTrailRepository;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void audit(PersistenceEntity entity, AuditParameter auditParameter) {
        if (!(entity instanceof EntityAuditTrail)) {
            createAuditTrail(entity, auditParameter.getOperation());
        }
    }

    protected void createAuditTrail(PersistenceEntity entity, PersistenceOperation operation) {
        try {
            EntityAuditTrail auditTrail = new EntityAuditTrail(
                    entity.getPrimaryKey().toString(),
                    entity.getClass().getSimpleName(),
                    operation.toString());
            auditTrailRepository.create(auditTrail);
        } catch (Exception ex) {
            LOGGER.error("Unable to persist audit trail for {}:{}",
                    entity.getClass().getSimpleName(), entity.getPrimaryKey(), ex);
        }
    }
}