package com.neo.util.framework.api.persitence.criteria;

import java.util.Date;

/**
 * If this Criteria is applied to a field, the field must be between one or both dates based on which ones are provided.
 */
public class DateSearchCriteria extends LongRangeSearchCriteria {

    protected String timeZone;

    public DateSearchCriteria(String fieldName, Date from, Date to, boolean not, String timeZone) {
        this(fieldName, from, to, not);
        this.setTimeZone(timeZone);
    }

    public DateSearchCriteria(String fieldName, Date from, Date to, boolean not) {
        super(
                fieldName,
                from != null ? from.getTime() : null,
                to != null ? to.getTime() : null,
                not);
    }

    public Date getFromDate() {
        return isIncludeFrom() ? new Date(getFrom()) : null;
    }

    public Date getToDate() {
        return isIncludeTo() ? new Date(getTo()) : null;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
