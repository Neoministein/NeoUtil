package com.neo.util.framework.database.api;

import jakarta.persistence.EntityManager;

public interface PersistenceContextService {

    EntityManager getEm();
}
