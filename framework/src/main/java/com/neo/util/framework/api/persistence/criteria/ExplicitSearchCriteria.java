package com.neo.util.framework.api.persistence.criteria;

/**
 * If this Criteria is applied to a field, the field must match the given value or not based on the boolean provided.
 * <p>
 * Wildcards:
 * <p>
 * *    Represents zero or more characters
 * <p>
 * ?    Represents a single character
 *
 */
public class ExplicitSearchCriteria extends FieldSearchCriteria {

    protected Object fieldValue;
    protected boolean allowWildcards;

    public ExplicitSearchCriteria(String fieldName, Object fieldValue, boolean allowWildcards, boolean not) {
        super(fieldName, not);
        this.fieldValue = fieldValue;
        this.allowWildcards = allowWildcards;
    }

    public ExplicitSearchCriteria(String fieldName, Object fieldValue, boolean allowWildcards) {
        this(fieldName, fieldValue, allowWildcards, false);
    }

    public ExplicitSearchCriteria(String fieldName, Object fieldValue) {
        this(fieldName, fieldValue, false);
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public boolean getAllowWildcards() {
        return allowWildcards;
    }
}
