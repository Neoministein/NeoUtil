package com.neo.javax.api.persitence.criteria;


/**
 * If this Criteria is applied to a field, the field must either exist with a value or or not,
 * depending on the booleans state.
 */
public class ExistingFieldSearchCriteria extends FieldSearchCriteria {

    protected final boolean exists;

    public ExistingFieldSearchCriteria(String fieldName, boolean exists) {
        super(fieldName);
        this.exists = exists;
    }

    public boolean getExists() {
        return exists;
    }

}
