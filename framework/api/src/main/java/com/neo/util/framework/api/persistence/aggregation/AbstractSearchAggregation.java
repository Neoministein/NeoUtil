package com.neo.util.framework.api.persistence.aggregation;

import java.io.Serializable;

/**
 * This defines an abstract aggregation on a field based on the given {@link AggregationType}
 */
public abstract class AbstractSearchAggregation implements SearchAggregation {

    /**
     * The aggregation types that are supported by the search provider.
     */
    public enum AggregationType {
        COUNT, SUM, AVG, MIN, MAX, CARDINALITY;
    }

    /* The aggregation type of this aggregation */
    private final AggregationType aggregationType;
    /* The name of this aggregation */
    private final String name;

    protected AbstractSearchAggregation(String name, AggregationType aggregationType) {
        this.aggregationType = aggregationType;
        this.name = name;
    }

    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public String getName() {
        return name;
    }
}
