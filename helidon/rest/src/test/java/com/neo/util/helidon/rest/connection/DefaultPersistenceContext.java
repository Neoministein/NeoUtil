package com.neo.util.helidon.rest.connection;

import com.neo.util.framework.database.api.PersistenceContextService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class DefaultPersistenceContext implements PersistenceContextService {

    @PersistenceContext(unitName = "mainPersistence")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
