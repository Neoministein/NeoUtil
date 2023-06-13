package com.neo.util.framework.database.persistence;

import com.fasterxml.jackson.annotation.JsonView;
import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;

import com.neo.util.framework.database.impl.DataBaseAuditListener;
import com.neo.util.framework.database.impl.InstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
@EntityListeners( { DataBaseAuditListener.class } )
public abstract class AuditableDataBaseEntity implements PersistenceEntity {

    public static final String C_CREATED_ON = "createdOn";
    public static final String C_CREATED_BY = "createdBy";

    public static final String C_TRANSACTION_COUNT = "transactionCount";
    public static final String C_UPDATED_ON = "updatedOn";
    public static final String C_UPDATED_BY = "updatedBy";

    @Convert(converter = InstantConverter.class)
    @Column(name = C_CREATED_ON, nullable = false, updatable = false)
        @JsonView(Views.Owner.class)
    protected Instant createdOn = Instant.now();

    @Column(name = C_CREATED_BY, nullable = false, updatable = false)
        @JsonView(Views.Internal.class)
    protected String createdBy;

    @Column(name = C_TRANSACTION_COUNT, nullable = false)
        @JsonView(Views.Internal.class)
    protected int transactionCount = 0;

    @Convert(converter = InstantConverter.class)
    @Column(name = C_UPDATED_ON, nullable = false)
        @JsonView(Views.Owner.class)
    protected Instant updatedOn = Instant.now();

    @Column(name = C_UPDATED_BY, nullable = false)
        @JsonView(Views.Owner.class)
    protected String updatedBy;

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
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

    public Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Instant updatedOn) {
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
        return JsonUtil.toJson(this);
    }
}
