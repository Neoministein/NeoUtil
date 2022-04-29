package com.neo.javax.api.persitence.aggregation;

/**
 * Defines aggregation result from a {@link SimpleFieldAggregation}
 */
public class SimpleAggregationResult extends AggregationResult {

    private final Object value;

    public SimpleAggregationResult(String name, Object value) {
        super(name);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

}
