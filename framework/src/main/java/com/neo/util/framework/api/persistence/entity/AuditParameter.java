package com.neo.util.framework.api.persistence.entity;

import com.neo.util.common.impl.enumeration.PersistenceOperation;

public class AuditParameter {

    /**
     * The operation
     */
    private final PersistenceOperation operation;

    public AuditParameter(PersistenceOperation operation) {
        this.operation = operation;
    }

    public PersistenceOperation getOperation() {
        return operation;
    }
}