package com.neo.util.framework.persistence.api;

import jakarta.persistence.EntityManager;

public interface PersistenceContextService {

    EntityManager getEntityManager();
}
