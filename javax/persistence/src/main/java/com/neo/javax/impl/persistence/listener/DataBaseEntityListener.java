package com.neo.javax.impl.persistence.listener;

import com.neo.javax.api.connection.RequestDetails;
import com.neo.javax.impl.persistence.entity.AbstractDataBaseEntity;

import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;
import java.util.UUID;

public class DataBaseEntityListener {

    protected static final String SYSTEM_INTERNAL = "internal";

    @Inject
    RequestDetails requestDetails;

    @PrePersist
    protected void prePersist(AbstractDataBaseEntity entity) {
        UUID uuid = requestDetails.getUUId();
        if (uuid != null) {
            entity.setCreatedBy(uuid.toString());
        } else {
            entity.setCreatedBy(SYSTEM_INTERNAL);
        }
        entity.setCreatedOn(new Date());
    }

    @PreUpdate
    protected void preUpdate(AbstractDataBaseEntity entity) {
        UUID uuid = requestDetails.getUUId();
        if (uuid != null) {
            entity.setUpdatedBy(uuid.toString());
        } else {
            entity.setUpdatedBy(SYSTEM_INTERNAL);
        }
        entity.setUpdatedOn(new Date());
        entity.setTransactionCount(entity.getTransactionCount() + 1);
    }
}
