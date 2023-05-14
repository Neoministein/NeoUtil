package com.neo.util.framework.database.impl.listener;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.database.impl.AuditableDataBaseEntity;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

public class DataBaseAuditListener {

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

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
}
