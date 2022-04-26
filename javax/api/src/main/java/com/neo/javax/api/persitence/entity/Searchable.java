package com.neo.javax.api.persitence.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.javax.api.persitence.IndexPeriod;

import java.util.Date;

/**
 * Defines necessary functionality of a class that can be indexed into a search provider.
 */
public interface Searchable {

    String BUSINESS_ID = "businessId";

    String TYPE = "type";

    /**
     * A value which is unique to this searchable
     *
     * @return a value which is unique to this searchable
     */
    String getBusinessId();

    /**
     * The index name in which the searchable should be stored
     *
     * @return the index name in which the searchable should be stored
     */
    String getIndexName();

    /**
     * The simple class name of the Searchable
     *
     * @return the simple class name of the Searchable
     */
    String getClassName();

    /**
     * The index period
     *
     * @return the index period
     */
    IndexPeriod getIndexPeriod();

    /**
     * The creation date
     *
     * @return the creation date
     */
    Date getCreationDate();

    /**
     * The Object in json format
     *
     * @return the Object in json format
     */
    ObjectNode getJsonNode();
}
