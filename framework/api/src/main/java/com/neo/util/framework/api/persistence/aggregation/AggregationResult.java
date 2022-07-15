package com.neo.util.framework.api.persistence.aggregation;

/**
 * Defines aggregation result from a search provider
 */
public abstract class AggregationResult {

    private final String name;

    protected AggregationResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
