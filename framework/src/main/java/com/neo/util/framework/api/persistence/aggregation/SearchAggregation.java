package com.neo.util.framework.api.persistence.aggregation;

import java.io.Serializable;

/**
 * An interface to aggregate data in searches
 */
public interface SearchAggregation extends Serializable {

    /**
     *  The name of this aggregations
     */
    String getName();
}
