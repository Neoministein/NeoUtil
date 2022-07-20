package com.neo.util.helidon.rest.connection;

import com.neo.util.framework.persistence.api.PersistenceContextService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class DefaultPersistenceContext implements PersistenceContextService {

    @PersistenceContext(unitName = "mainPersistence")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
