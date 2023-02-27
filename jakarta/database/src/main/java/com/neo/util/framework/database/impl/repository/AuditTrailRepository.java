package com.neo.util.framework.database.impl.repository;

import com.neo.util.framework.database.impl.EntityAuditTrail;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditTrailRepository extends EntityRepositoryImpl<EntityAuditTrail> {

    public AuditTrailRepository() {
        super(EntityAuditTrail.class);
    }
}
