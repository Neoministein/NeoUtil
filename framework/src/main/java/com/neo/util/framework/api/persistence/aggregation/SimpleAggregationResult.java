package com.neo.util.framework.api.persistence.aggregation;

/**
 * Defines simpleFieldAggregation result from a {@link SimpleFieldAggregation}
 */
public record SimpleAggregationResult(String name, Object value) implements AggregationResult {

    @Override
    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
