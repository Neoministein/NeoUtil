package com.neo.util.framework.api.persistence.aggregation;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * The result of {@link CriteriaAggregation}
 */
public record CriteriaAggregationResult(
                                        @JsonIgnore
                                        String name,
                                        @JsonAnyGetter
                                        Map<String, Object> criteriaResult) implements AggregationResult {

    public Map<String, Object> getCriteriaResult() {
        return criteriaResult;
    }

    @Override
    public String getName() {
        return name;
    }
}
