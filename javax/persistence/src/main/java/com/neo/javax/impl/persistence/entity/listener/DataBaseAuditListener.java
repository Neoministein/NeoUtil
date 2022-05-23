package com.neo.javax.impl.persistence.entity.listener;

import com.neo.javax.api.connection.RequestDetails;
import com.neo.javax.impl.persistence.entity.AuditableDataBaseEntity;

import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class DataBaseAuditListener {

    @Inject
    RequestDetails requestDetails;

    @PrePersist
    protected void prePersist(AuditableDataBaseEntity entity) {
        Optional<UUID> uuid = requestDetails.getUUId();
        if (uuid.isPresent()) {
            setPersistData(entity,uuid.get().toString());
            setUpdateData(entity, uuid.get().toString());
        } else {
            setPersistData(entity, requestDetails.getRequestId());
            setUpdateData(entity, requestDetails.getRequestId());
        }
    }

    @PreUpdate
    protected void preUpdate(AuditableDataBaseEntity entity) {
        Optional<UUID> uuid = requestDetails.getUUId();
        if (uuid.isPresent()) {
            setUpdateData(entity, uuid.get().toString());
        } else {
            setUpdateData(entity, requestDetails.getRequestId());
        }
    }

    protected void setPersistData(AuditableDataBaseEntity entity, String by) {
        entity.setCreatedBy(by);
        entity.setCreatedOn(new Date());
    }

    protected void setUpdateData(AuditableDataBaseEntity entity, String by) {
        entity.setTransactionCount(entity.getTransactionCount()+1);
        entity.setUpdatedBy(by);
        entity.setUpdatedOn(new Date());
    }
}
