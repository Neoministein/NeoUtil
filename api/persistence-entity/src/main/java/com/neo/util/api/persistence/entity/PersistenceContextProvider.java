package com.neo.util.api.persistence.entity;

import jakarta.persistence.EntityManager;

public interface PersistenceContextProvider {

    EntityManager getEm();
}
