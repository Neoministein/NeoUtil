package com.neo.util.helidon.rest.connection;

import com.neo.util.framework.database.api.PersistenceContextProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class DefaultPersistenceContext implements PersistenceContextProvider {

    @PersistenceContext(unitName = "mainPersistence")
    private EntityManager em;

    @Override
    public EntityManager getEm() {
        return em;
    }
}
