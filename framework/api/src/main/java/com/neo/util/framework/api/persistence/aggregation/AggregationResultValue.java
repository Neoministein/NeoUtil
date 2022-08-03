package com.neo.util.framework.api.persistence.aggregation;

import java.util.Map;

/**
 * Defines an aggregation result value from a {@link ComplexAggregationResult}. The object contains a map with all the
 * grouped column values and the calculated value for this combination.
 */
public record AggregationResultValue(Map<String, Object> columnValues, Object value) {

}
