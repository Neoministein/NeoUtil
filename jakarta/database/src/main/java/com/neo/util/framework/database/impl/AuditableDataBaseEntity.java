package com.neo.util.framework.database.impl;

import com.fasterxml.jackson.annotation.JsonView;
import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.impl.listener.DataBaseAuditListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
@EntityListeners( { DataBaseAuditListener.class } )
public abstract class AuditableDataBaseEntity implements PersistenceEntity {

    public static final String C_CREATED_ON = "createdOn";
    public static final String C_CREATED_BY = "createdBy";

    public static final String C_TRANSACTION_COUNT = "transactionCount";
    public static final String C_UPDATED_ON = "updatedOn";
    public static final String C_UPDATED_BY = "updatedBy";

    @Column(name = C_CREATED_ON, nullable = false, updatable = false)
        @JsonView(Views.Owner.class)
    protected Date createdOn = new Date();

    @Column(name = C_CREATED_BY, nullable = false, updatable = false)
        @JsonView(Views.Internal.class)
    protected String createdBy;

    @Column(name = C_TRANSACTION_COUNT, nullable = false)
        @JsonView(Views.Internal.class)
    protected int transactionCount = 0;

    @Column(name = C_UPDATED_ON, nullable = false)
        @JsonView(Views.Owner.class)
    protected Date updatedOn = new Date();

    @Column(name = C_UPDATED_BY, nullable = false)
        @JsonView(Views.Owner.class)
    protected String updatedBy;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getTransactionCount() {
        return transactionCount;
    }


    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "\"" + this.getClass().getSimpleName() + "\":" + JsonUtil.toJson(this);
    }
}
