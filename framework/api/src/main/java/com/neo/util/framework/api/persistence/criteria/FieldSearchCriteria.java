package com.neo.util.framework.api.persistence.criteria;


/**
 * This Criteria is abstract and is meant to be used used as a base of a criteria which targets a specific field.
 */
public abstract class FieldSearchCriteria implements SearchCriteria {

    protected String fieldName;
    protected boolean not;

    protected FieldSearchCriteria(String fieldName) {
        this(fieldName, false);
    }

    protected FieldSearchCriteria(String fieldName, boolean not) {
        this.fieldName = fieldName;
        this.not = not;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

}
