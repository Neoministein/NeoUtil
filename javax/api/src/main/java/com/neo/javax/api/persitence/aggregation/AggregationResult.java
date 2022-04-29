package com.neo.javax.api.persitence.aggregation;

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

