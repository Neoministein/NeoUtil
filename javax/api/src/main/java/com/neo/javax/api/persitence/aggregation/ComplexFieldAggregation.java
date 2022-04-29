package com.neo.javax.api.persitence.aggregation;

import java.util.List;

/**
 * Defines a complex aggregation request over multiple fields. This is similar to an SQL ORDER BY statement. The
 * fieldName is the column name that will be aggregated on. The groupFields are used for the "GROUP BY".
 */
public class ComplexFieldAggregation extends SimpleFieldAggregation {

    public static final int DEFAULT_AGGREGATION_SIZE = 10;

    private final List<String> groupFields;

    private final int size;

    public ComplexFieldAggregation(String name, String aggregationFieldName, List<String> groupFields) {
        this(name, AggregationType.COUNT, aggregationFieldName, groupFields);
    }

    public ComplexFieldAggregation(String name, AggregationType aggregationType, String fieldName,
            List<String> groupFields) {
        this(name, aggregationType, fieldName, groupFields, DEFAULT_AGGREGATION_SIZE);
    }

    public ComplexFieldAggregation(String name, AggregationType aggregationType, String fieldName,
            List<String> groupFields, int size) {
        super(name, fieldName, aggregationType);
        this.groupFields = groupFields;
        this.size = size;
    }

    public List<String> getGroupFields() {
        return groupFields;
    }

    public int getSize() {
        return size;
    }


}
