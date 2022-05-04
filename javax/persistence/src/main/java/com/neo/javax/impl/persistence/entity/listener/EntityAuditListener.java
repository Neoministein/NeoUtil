package com.neo.javax.impl.persistence.entity.listener;

import com.neo.common.impl.enumeration.PersistenceOperation;
import com.neo.javax.api.persitence.entity.EntityRepository;
import com.neo.javax.impl.persistence.entity.AbstractDataBaseEntity;
import com.neo.javax.impl.persistence.entity.EntityAuditTrail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.transaction.RollbackException;

public class EntityAuditListener {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EntityAuditListener.class);

    @Inject
    EntityRepository entityRepository;

    @PrePersist
    public void prePersist(AbstractDataBaseEntity entity) {
        if (!(entity instanceof EntityAuditTrail)) {
            createAuditTrail(entity, PersistenceOperation.CREATE);
        }
    }

    @PreUpdate
    public void preUpdate(AbstractDataBaseEntity entity) {
        createAuditTrail(entity, PersistenceOperation.UPDATE);
    }

    @PreRemove
    public void preRemove(AbstractDataBaseEntity entity) {
        createAuditTrail(entity, PersistenceOperation.DELETE);
    }

    protected void createAuditTrail(AbstractDataBaseEntity entity, PersistenceOperation operation) {
        try {
            EntityAuditTrail auditTrail = new EntityAuditTrail();
            auditTrail.setOperation(operation);
            auditTrail.setClassType(entity.getClass().getSimpleName());
            auditTrail.setObjectKey(entity.getPrimaryKey().toString());
            entityRepository.create(auditTrail);
        } catch (RollbackException ex) {
            String entityIdentifier = entity.getClass().getSimpleName() + ":" + entity.getPrimaryKey();
            LOGGER.error("Unable to persist audit trail for {}", entityIdentifier, ex);
        }
    }

}
