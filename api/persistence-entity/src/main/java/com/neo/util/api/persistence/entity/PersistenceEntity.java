package com.neo.util.api.persistence.entity;

import java.io.Serializable;

/**
 * An interface which enables easy working with unknown database entities
 */
public interface PersistenceEntity<T> extends Serializable {

    String C_ID = "id";

    /**
     * The entities primary key
     */
    T getPrimaryKey();

}
