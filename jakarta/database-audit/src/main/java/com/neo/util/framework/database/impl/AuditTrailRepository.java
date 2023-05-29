package com.neo.util.framework.database.impl;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditTrailRepository extends AbstractEntityRepository<EntityAuditTrail> {

    public AuditTrailRepository() {
        super(EntityAuditTrail.class);
    }
}
