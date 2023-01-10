package com.neo.util.framework.api.persistence.aggregation;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines aggregations result from a {@link SimpleFieldAggregation}
 */
public record SimpleAggregationResult(String name, Object value) implements AggregationResult {

    @Override
    public String getName() {
        return name;
    }

    @JsonValue
    public Object getValue() {
        return value;
    }
}
