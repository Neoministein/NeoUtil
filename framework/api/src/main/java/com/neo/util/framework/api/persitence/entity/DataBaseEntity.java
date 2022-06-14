package com.neo.util.framework.api.persitence.entity;

import java.io.Serializable;

/**
 * A Interface which enables easy working with unknown database entities
 */
public interface DataBaseEntity extends Serializable {

    String C_ID = "id";

    /**
     * The entities primary key
     */
    Object getPrimaryKey();

}
