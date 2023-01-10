package com.neo.util.framework.api.persistence.aggregation;

import java.util.List;
import java.util.Optional;

/**
 * The {@link TermAggregation} is similar to a SQL OrderBy clause
 */
public record TermAggregation(String name, String fieldName, Integer maxResult, String aggregationToOrderAfter, Boolean asc, List<SearchAggregation> aggregations) implements SearchAggregation {


    public static final int DEFAULT_MAX_RESULT = 10;

    public TermAggregation(String name, String fieldName, String aggregationToOrderAfter, Boolean asc, List<SearchAggregation> aggregations) {
        this(name, fieldName, DEFAULT_MAX_RESULT, aggregationToOrderAfter, asc, aggregations);
    }

    public TermAggregation(String name, String fieldName, Integer maxResult, List<SearchAggregation> aggregations) {
        this(name, fieldName, maxResult, null, null, aggregations);
    }

    public TermAggregation(String name, String fieldName, List<SearchAggregation> aggregations) {
        this(name, fieldName, DEFAULT_MAX_RESULT, null, null, aggregations);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Optional<Integer> getMaxResults() {
        return Optional.ofNullable(maxResult);
    }

    public Optional<String> getAggregationToOrderAfter() {
        return Optional.ofNullable(aggregationToOrderAfter);
    }

    public Optional<Boolean> isAsc() {
        return Optional.ofNullable(asc);
    }

    public List<SearchAggregation> getSimpleFieldAggregation() {
        return aggregations;
    }
}
