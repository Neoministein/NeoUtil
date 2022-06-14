package com.neo.util.framework.api.persitence.criteria;

/**
 * If this Criteria is applied to a field, the field must be between one or both long values based on which ones are provided.
 */
public class LongRangeSearchCriteria extends RangeBasedSearchCriteria {

    protected Long from;
    protected Long to;

    public LongRangeSearchCriteria(String fieldName, Long from, Long to, boolean not) {
        super(fieldName, not);
        this.from = from;
        this.to = to;
    }

    @Override
    public Long getFrom() {
        return from;
    }

    @Override
    public Long getTo() {
        return to;
    }
}
