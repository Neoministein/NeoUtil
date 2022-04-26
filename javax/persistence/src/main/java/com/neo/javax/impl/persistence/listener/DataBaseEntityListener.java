package com.neo.javax.impl.persistence.listener;

import com.neo.javax.api.connection.RequestDetails;
import com.neo.javax.impl.persistence.entity.AbstractDataBaseEntity;

import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class DataBaseEntityListener {

    @Inject
    RequestDetails requestDetails;

    @PrePersist
    protected void prePersist(AbstractDataBaseEntity entity) {
        Optional<UUID> uuid = requestDetails.getUUId();
        if (uuid.isPresent()) {
            setPersistData(entity,uuid.toString());
            setUpdateData(entity, uuid.toString());
        } else {
            setPersistData(entity, requestDetails.getRemoteAddress());
            setUpdateData(entity, requestDetails.getRemoteAddress());
        }
    }

    @PreUpdate
    protected void preUpdate(AbstractDataBaseEntity entity) {
        Optional<UUID> uuid = requestDetails.getUUId();
        if (uuid.isPresent()) {
            setUpdateData(entity, uuid.get().toString());
        } else {
            setUpdateData(entity, requestDetails.getRequestId());
        }
    }

    protected void setPersistData(AbstractDataBaseEntity entity, String by) {
        entity.setCreatedBy(by);
        entity.setCreatedOn(new Date());
    }

    protected void setUpdateData(AbstractDataBaseEntity entity, String by) {
        entity.setTransactionCount(entity.getTransactionCount()+1);
        entity.setUpdatedBy(by);
        entity.setUpdatedOn(new Date());
    }
}
