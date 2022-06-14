package com.neo.util.framework.persistence.api;

import javax.persistence.EntityManager;

public interface PersistenceContextService {

    EntityManager getEntityManager();
}
