package com.neo.util.framework.api.persistence.criteria;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * If this Criteria is applied to a field, the field must match one of the given values or is not allowed to match any exist with a values,
 * depending on the booleans state.
 */
public class ContainsSearchCriteria extends FieldSearchCriteria {

    protected final List<Serializable> fieldValues;

    public ContainsSearchCriteria(String fieldName, Serializable... fieldValues) {
        this(fieldName, false, fieldValues);
    }


    public ContainsSearchCriteria(String fieldName, boolean not, Serializable... fieldValues) {
        super(fieldName, not);
        this.fieldValues = Arrays.asList(fieldValues);
    }

    public ContainsSearchCriteria(String fieldName, boolean not, List<Serializable> fieldValueSerializableList) {
        super(fieldName, not);
        this.fieldValues = fieldValueSerializableList;
    }

    public List<Serializable> getFieldValues() {
        return fieldValues;
    }
}
