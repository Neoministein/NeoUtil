package com.neo.util.framework.api.persistence.entity;

import com.neo.util.common.impl.enumeration.PersistenceOperation;

public class AuditParameter {

    /**
     * The operation
     */
    private final PersistenceOperation operation;

    public AuditParameter(PersistenceOperation synchronization) {
        this.operation = synchronization;
    }

    public PersistenceOperation getOperation() {
        return operation;
    }
}