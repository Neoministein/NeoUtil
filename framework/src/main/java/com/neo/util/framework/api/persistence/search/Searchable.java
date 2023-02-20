package com.neo.util.framework.api.persistence.search;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonUtil;
import jakarta.enterprise.context.Dependent;

import java.util.Date;

/**
 * Defines necessary functionality of a class that can be indexed into a search provider.
 * <p>
 * Classes implementing this interface need to have the {@link Dependent}.
 * Even though the get instantiated through the new keyword
 */
public interface Searchable {

    String BUSINESS_ID = "businessId";

    String INDEX_SEARCH_KEYWORD = ".keyword";

    String TYPE = "searchable";

    /**
     * A value which is unique to this searchable
     */
    String getBusinessId();

    /**
     * The index name in which the searchable should be stored
     */
    String getIndexName();


    /**
     * The json to store
     */
    default ObjectNode getObjectNode() {
        try {
            ObjectNode objectNode = JsonUtil.fromPojo(this);
            if (!StringUtils.isEmpty(getBusinessId())) {
                objectNode.put(Searchable.BUSINESS_ID, getBusinessId());
            }
            objectNode.put(Searchable.TYPE, getClassName());
            return objectNode;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Error while parsing searchable to json: " + getClassName() + ":" + getBusinessId(), ex);
        }
    }

    /**
     * The simple class name of the Searchable
     */
    default String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * The index period
     */
    default IndexPeriod getIndexPeriod() {
        return IndexPeriod.DEFAULT;
    }

    /**
     * The creation date
     */
    Date getCreationDate();

    /**
     * The transaction count
     */
    Long getVersion();
}
