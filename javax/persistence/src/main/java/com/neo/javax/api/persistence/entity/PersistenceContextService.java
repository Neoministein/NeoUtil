package com.neo.javax.api.persistence.entity;

import javax.persistence.EntityManager;

public interface PersistenceContextService {

    EntityManager getEntityManager();
}
