package com.neo.util.framework.api.persistence.aggregation;

import java.util.Map;

/**
 * Defines an aggregation result value from a {@link ComplexAggregationResult}. The object contains a map with all the
 * grouped column values and the calculated value for this combination.
 */
public class AggregationResultValue {

    private final Object value;
    private final Map<String, Object> columnValues;

    public AggregationResultValue(Map<String, Object> columnValues, Object value) {
        this.columnValues = columnValues;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Map<String, Object> getColumnValues() {
        return columnValues;
    }

}
