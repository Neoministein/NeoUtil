package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.enumeration.PersistenceOperation;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.api.PersistenceContextProvider;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractDatabaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseRepository.class);

    @Inject
    protected PersistenceContextProvider pcs;

    protected <X extends PersistenceEntity> Optional<X> find(Object primaryKey, Class<X> entityClazz) {
        try {
            LOGGER.trace("Searching for entity {}:{}", entityClazz.getSimpleName(), primaryKey);
            return Optional.ofNullable(pcs.getEm().find(entityClazz, primaryKey));
        } catch (NoResultException | IllegalArgumentException  ex) {
            LOGGER.trace("Unable to find entity {}:{}", entityClazz.getSimpleName(), primaryKey);
            return Optional.empty();
        }
    }

    protected void createWithAudit(PersistenceEntity entity) {
        pcs.getEm().persist(entity);
        LOGGER.debug("Created entity {}:{}", entity.getClass().getSimpleName(),  entity);
        if (!(entity instanceof EntityAuditTrail)) {
            createAuditTrail(entity, PersistenceOperation.CREATE);
        }
    }

    protected void editWithAudit(PersistenceEntity entity) {
        pcs.getEm().merge(entity);
        LOGGER.debug("Edited entity {}:{}",entity.getClass().getSimpleName(), entity);
        createAuditTrail(entity, PersistenceOperation.UPDATE);
    }

    protected void removeWithAudit(PersistenceEntity entity) {
        pcs.getEm().remove(pcs.getEm().merge(entity));
        LOGGER.debug("Removed entity {}:{}",entity.getClass().getSimpleName(), entity);
        createAuditTrail(entity, PersistenceOperation.DELETE);
    }

    protected void createAuditTrail(PersistenceEntity entity, PersistenceOperation operation) {
        try {
            EntityAuditTrail auditTrail = new EntityAuditTrail();
            auditTrail.setOperation(operation);
            auditTrail.setClassType(entity.getClass().getSimpleName());
            auditTrail.setObjectKey(entity.getPrimaryKey().toString());
            this.createWithAudit(auditTrail);
        } catch (Exception ex) {
            LOGGER.error("Unable to persist audit trail for {}:{}",
                    entity.getClass().getSimpleName(), entity.getPrimaryKey(), ex);
        }
    }
}
