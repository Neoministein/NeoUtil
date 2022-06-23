package com.neo.util.framework.api.persistence.criteria;

import java.io.Serializable;

/**
 * If this Criteria is applied to a field, the field must be between one or both values based on which ones are provided.
 */
public abstract class RangeBasedSearchCriteria extends ExplicitSearchCriteria {

    protected RangeBasedSearchCriteria(String fieldName, boolean not) {
        super(fieldName, null, not);
    }

    public abstract Serializable getFrom();

    public abstract Serializable getTo();

    public boolean isIncludeFrom() {
        return getFrom() != null;
    }

    public boolean isIncludeTo() {
        return getTo() != null;
    }
}
