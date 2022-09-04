package com.neo.util.framework.api.persistence.aggregation;

import java.util.Map;

/**
 * The result of {@link CriteriaAggregation}
 */
public record CriteriaAggregationResult(String name, Map<String, Object> criteriaResult) implements AggregationResult {

    public Map<String, Object> getCriteriaResult() {
        return criteriaResult;
    }

    @Override
    public String getName() {
        return name;
    }
}
