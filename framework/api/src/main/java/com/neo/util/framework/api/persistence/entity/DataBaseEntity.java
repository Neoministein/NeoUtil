package com.neo.util.framework.api.persistence.entity;

import java.io.Serializable;

/**
 * An interface which enables easy working with unknown database entities
 */
public interface DataBaseEntity extends Serializable {

    String C_ID = "id";

    /**
     * The entities primary key
     */
    Object getPrimaryKey();

}
