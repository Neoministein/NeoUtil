package com.neo.util.jakarta.database.audit;

import com.neo.util.jakarta.database.PersistenceContextProvider;
import com.neo.util.jakarta.database.repository.AbstractDatabaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuditTrailRepository extends AbstractDatabaseRepository<Long, EntityAuditTrail> {

    @Inject
    public AuditTrailRepository(PersistenceContextProvider pcp) {
        super(pcp, EntityAuditTrail.class);
    }
}
