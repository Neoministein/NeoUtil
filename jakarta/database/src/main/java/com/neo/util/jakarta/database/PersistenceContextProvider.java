package com.neo.util.jakarta.database;

import jakarta.persistence.EntityManager;

public interface PersistenceContextProvider {

    EntityManager getEm();
}
