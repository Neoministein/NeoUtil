package com.neo.util.framework.api.persitence.criteria;

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

    @Override
    public Double getFrom() {
        return from;
    }

    @Override
    public Double getTo() {
        return to;
    }
}
