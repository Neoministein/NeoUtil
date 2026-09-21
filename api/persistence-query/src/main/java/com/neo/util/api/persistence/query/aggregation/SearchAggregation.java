package com.neo.util.api.persistence.query.aggregation;

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
