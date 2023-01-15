package com.neo.util.framework.api.persistence.aggregation;

import java.util.List;
import java.util.Optional;

/**
 * The {@link TermAggregation} is similar to a SQL OrderBy clause
 */
public record TermAggregation(String name, String fieldName, Integer maxResult, Order order, Partition partition,
                              List<SearchAggregation> aggregations) implements SearchAggregation {

    public static final int DEFAULT_MAX_RESULT = 10;

    public record Partition(long partition, long numPartition) {}

    public record Order(String aggregationToOrderAfter, boolean asc) {
        public Order(String aggregationToOrderAfter) {
            this(aggregationToOrderAfter, false);
        }
    }

    public TermAggregation(String name, String fieldName, Order order, List<SearchAggregation> aggregations) {
        this(name, fieldName, DEFAULT_MAX_RESULT, order, null, aggregations);
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

    public Optional<Integer> getMaxResult() {
        return Optional.ofNullable(maxResult);
    }

    public Optional<Order> getOrder() {
        return Optional.ofNullable(order);
    }

    public Optional<Partition> getPartition() {
        return Optional.ofNullable(partition);
    }

    public List<SearchAggregation> getSimpleFieldAggregation() {
        return aggregations;
    }
}
