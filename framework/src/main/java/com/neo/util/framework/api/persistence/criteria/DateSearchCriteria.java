package com.neo.util.framework.api.persistence.criteria;

import java.time.Instant;
import java.time.ZoneId;

/**
 * If this Criteria is applied to a field, the field must be between one or both dates based on which ones are provided.
 */
public class DateSearchCriteria extends LongRangeSearchCriteria {

    protected ZoneId timeZone;

    public DateSearchCriteria(String fieldName, Instant from, Instant to, boolean not, ZoneId timeZone) {
        this(fieldName, from, to, not);
        this.setTimeZone(timeZone);
    }

    public DateSearchCriteria(String fieldName, Instant from, Instant to, boolean not) {
        super(
                fieldName,
                from != null ? from.toEpochMilli() : null,
                to != null ? to.toEpochMilli() : null,
                not);
    }

    public DateSearchCriteria(String fieldName, Instant from, Instant to) {
        this(fieldName, from, to, false);
    }

    public Instant getFromDate() {
        return isIncludeFrom() ? Instant.ofEpochMilli(getFrom()) : null;
    }

    public Instant getToDate() {
        return isIncludeTo() ? Instant.ofEpochMilli(getTo()) : null;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }
}
