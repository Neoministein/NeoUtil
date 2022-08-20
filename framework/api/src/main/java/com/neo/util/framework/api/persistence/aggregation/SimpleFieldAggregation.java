package com.neo.util.framework.api.persistence.aggregation;

/**
 * Defines a simple simpleFieldAggregation over a given field.
 */
public record SimpleFieldAggregation(String name, String fieldName, Type type) implements SearchAggregation {

    /**
     * The simpleFieldAggregation types that are supported by the search provider.
     */
    public enum Type {
        COUNT, SUM, AVG, MIN, MAX, CARDINALITY;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Type getType() {
        return type;
    }

}
