package com.neo.util.framework.api.persistence.entity;

/**
 * This interfaces defines the capability for a basic audit trail
 */
public interface EntityAuditTrailProvider {

    /**
     * Saves the audit event for the given entity and parameters
     *
     * @param entity which have been changed
     * @param auditParameter parameters for the audit trial
     */
    void audit(PersistenceEntity entity, AuditParameter auditParameter);
}
