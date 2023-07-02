package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.enumeration.PersistenceOperation;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.persistence.entity.AuditParameter;
import com.neo.util.framework.api.persistence.entity.AuditTrailProvider;

import com.neo.util.framework.database.persistence.AuditableDataBaseEntity;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.*;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.time.Instant;

public class DataBaseAuditListener {

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @Inject
    protected AuditTrailProvider auditTrailProvider;

    @Inject
    private TransactionSynchronizationRegistry registry;

    @PrePersist
    protected void prePersist(AuditableDataBaseEntity entity) {
        final RequestDetails requestDetails = requestDetailsProvider.get();
        setPersistData(entity,requestDetails.getCaller());
        setUpdateData(entity, requestDetails.getCaller());
    }

    @PreUpdate
    protected void preUpdate(AuditableDataBaseEntity entity) {
        setUpdateData(entity, requestDetailsProvider.get().getCaller());
    }

    protected void setPersistData(AuditableDataBaseEntity entity, String by) {
        entity.setCreatedBy(by);
        entity.setCreatedOn(Instant.now());
    }

    protected void setUpdateData(AuditableDataBaseEntity entity, String by) {
        entity.setTransactionCount(entity.getTransactionCount()+1);
        entity.setUpdatedBy(by);
        entity.setUpdatedOn(Instant.now());
    }
    @PostPersist
    protected void postPersist(AuditableDataBaseEntity entity) {
        registry.registerInterposedSynchronization(new AfterCommitAction(() ->
                auditTrailProvider.audit(entity, new AuditParameter(PersistenceOperation.CREATE))));
    }

    @PostUpdate
    protected void postUpdate(AuditableDataBaseEntity entity) {
        registry.registerInterposedSynchronization(new AfterCommitAction(() ->
                auditTrailProvider.audit(entity, new AuditParameter(PersistenceOperation.UPDATE))));
    }

    @PostRemove
    protected void postRemove(AuditableDataBaseEntity entity) {
        registry.registerInterposedSynchronization(new AfterCommitAction(() ->
                auditTrailProvider.audit(entity, new AuditParameter(PersistenceOperation.DELETE))));
    }

    public record AfterCommitAction(Runnable action) implements Synchronization {
        @Override
        public void beforeCompletion() {

        }

        @Override
        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                action.run();
            }
        }
    }
}
