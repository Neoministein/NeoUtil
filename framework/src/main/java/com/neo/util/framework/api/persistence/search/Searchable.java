package com.neo.util.framework.api.persistence.search;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.build.SearchableAnnotationBuildStep;

import java.time.Instant;

/**
 * Defines necessary functionality of a class that can be indexed into a search provider.
 * <p>
 * Classes implementing this interface need to have the {@link SearchableIndex}.
 * It's validated via the {@link BuildStep}: {@link SearchableAnnotationBuildStep}
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
     * The creation date
     */
    Instant getCreationDate();

    /**
     * The transaction count
     */
    Long getVersion();
}
