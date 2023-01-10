package com.neo.util.framework.api.persistence.aggregation;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.List;
import java.util.Map;
/**
 * Defines the result from a {@link TermAggregation}
 */
public record TermAggregationResult(String name, List<Bucket> buckets) implements AggregationResult {


    @Override
    public String getName() {
        return name;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public record Bucket(
            String key,
            @JsonAnyGetter
            Map<String, Object> aggregationResults) {}
}
