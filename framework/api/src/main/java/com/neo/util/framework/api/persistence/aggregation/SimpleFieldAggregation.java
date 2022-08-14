package com.neo.util.framework.api.persistence.aggregation;

/**
 * Defines a simple aggregation over a given field.
 */
public class SimpleFieldAggregation extends AbstractSearchAggregation {

    private final String fieldName;

    public SimpleFieldAggregation(String name, String fieldName) {
        this(name, fieldName, AggregationType.COUNT);
    }

    public SimpleFieldAggregation(String name, String fieldName, AggregationType type) {
        super(name, type);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

}
