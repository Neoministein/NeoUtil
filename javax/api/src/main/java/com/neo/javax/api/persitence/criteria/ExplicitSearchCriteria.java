package com.neo.javax.api.persitence.criteria;

/**
 * If this Criteria is applied to a field, the field must match the given value or not based on the boolean provided.
 */
public class ExplicitSearchCriteria extends FieldSearchCriteria {

    protected Object fieldValue;

    public ExplicitSearchCriteria(String fieldName, Object fieldValue, boolean not) {
        super(fieldName, not);
        this.fieldValue = fieldValue;
    }

    public ExplicitSearchCriteria(String fieldName, Object fieldValue) {
        this(fieldName, fieldValue, false);
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
