package com.neo.util.framework.api.persistence.criteria;

/**
 * If this Criteria is applied to a field, the field must be between one or both number values based on which ones are provided.
 */
public class DoubleRangeSearchCriteria extends RangeBasedSearchCriteria {

    protected Double from;
    protected Double to;

    public DoubleRangeSearchCriteria(String fieldName, Double from, Double to, boolean not) {
        super(fieldName, not);
        this.from = from;
        this.to = to;
    }

    public DoubleRangeSearchCriteria(String fieldName, Double from, Double to) {
        this(fieldName, from, to, false);
    }

    @Override
    public Double getFrom() {
        return from;
    }

    @Override
    public Double getTo() {
        return to;
    }
}
