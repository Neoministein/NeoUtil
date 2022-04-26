package com.neo.javax.api.persistence.repository;

import javax.persistence.EntityManager;

public interface PersistenceContextService {

    EntityManager getEntityManager();
}
