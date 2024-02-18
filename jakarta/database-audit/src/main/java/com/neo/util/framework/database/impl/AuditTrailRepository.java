package com.neo.util.framework.database.impl;

import com.neo.util.framework.database.api.PersistenceContextProvider;
import com.neo.util.framework.database.persistence.EntityAuditTrail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuditTrailRepository extends AbstractDatabaseRepository<EntityAuditTrail> {

    @Inject
    public AuditTrailRepository(PersistenceContextProvider pcp) {
        super(pcp, EntityAuditTrail.class);
    }
}
