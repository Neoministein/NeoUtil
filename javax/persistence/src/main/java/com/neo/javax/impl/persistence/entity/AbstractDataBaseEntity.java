package com.neo.javax.impl.persistence.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.neo.common.api.json.Views;
import com.neo.javax.api.persitence.entity.DataBaseEntity;
import com.neo.javax.impl.persistence.entity.listener.DataBaseEntityListener;
import com.neo.javax.impl.persistence.entity.listener.EntityAuditListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
@EntityListeners( { DataBaseEntityListener.class, EntityAuditListener.class } )
public abstract class AbstractDataBaseEntity implements DataBaseEntity {

    public static final String C_CREATED_ON = "created_on";
    public static final String C_CREATED_BY = "created_by";

    public static final String C_TRANSACTION_COUNT = "transaction_count";
    public static final String C_UPDATED_ON = "updated_on";
    public static final String C_UPDATED_BY = "updated_by";

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
}
