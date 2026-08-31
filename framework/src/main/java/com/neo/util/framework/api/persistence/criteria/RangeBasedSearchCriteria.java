package com.neo.util.framework.api.persistence.criteria;

/**
 * If this Criteria is applied to a field, the field must be between one or both values based on which ones are provided.
 */
public abstract class RangeBasedSearchCriteria extends FieldSearchCriteria {

    protected RangeBasedSearchCriteria(String fieldName, boolean not) {
        super(fieldName, not);
    }

    public abstract Number getFrom();

    public abstract Number getTo();

    public boolean isIncludeFrom() {
        return getFrom() != null;
    }

    public boolean isIncludeTo() {
        return getTo() != null;
    }
}
