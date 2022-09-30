package com.neo.util.framework.database.impl.listener;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.database.impl.AuditableDataBaseEntity;

import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Date;

public class DataBaseAuditListener {

    @Inject
    protected RequestDetails requestDetails;

    @PrePersist
    protected void prePersist(AuditableDataBaseEntity entity) {
        setPersistData(entity,requestDetails.getCaller());
        setUpdateData(entity, requestDetails.getCaller());
    }

    @PreUpdate
    protected void preUpdate(AuditableDataBaseEntity entity) {
        setUpdateData(entity, requestDetails.getCaller());
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
