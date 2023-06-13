package com.neo.util.framework.database.impl;

import com.neo.util.framework.database.persistence.EntityAuditTrail;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditTrailRepository extends AbstractDatabaseRepository<EntityAuditTrail> {

    public AuditTrailRepository() {
        super(EntityAuditTrail.class);
    }
}
