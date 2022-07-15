package com.neo.util.framework.persistence.impl.listener;

import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.persistence.impl.AuditableDataBaseEntity;

import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;
import java.util.Optional;

public class DataBaseAuditListener {

    @Inject
    RequestDetails requestDetails;

    @PrePersist
    protected void prePersist(AuditableDataBaseEntity entity) {
        Optional<RolePrincipal> uuid = requestDetails.getUser();
        if (uuid.isPresent()) {
            setPersistData(entity,uuid.get().getName());
            setUpdateData(entity, uuid.get().getName());
        } else {
            setPersistData(entity, requestDetails.getRequestId());
            setUpdateData(entity, requestDetails.getRequestId());
        }
    }

    @PreUpdate
    protected void preUpdate(AuditableDataBaseEntity entity) {
        Optional<RolePrincipal> uuid = requestDetails.getUser();
        if (uuid.isPresent()) {
            setUpdateData(entity, uuid.get().getName());
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
