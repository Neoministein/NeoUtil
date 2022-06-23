package com.neo.util.framework.api.persistence.aggregation;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines aggregation result from a {@link ComplexFieldAggregation}
 */
public class ComplexAggregationResult extends AggregationResult {

    private final List<AggregationResultValue> values = new ArrayList<>();

    public ComplexAggregationResult(String name) {
        super(name);
    }

    public void addValue(AggregationResultValue value) {
        values.add(value);
    }

    public List<AggregationResultValue> getValues() {
        return values;
    }

}
