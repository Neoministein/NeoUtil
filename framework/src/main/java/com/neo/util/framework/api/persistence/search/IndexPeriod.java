package com.neo.util.framework.api.persistence.search;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

/**
 * The period for an index. This will be used to decided when new indexes have to be created.
 * Do not change a IndexPeriod for an entity on a running system.
 */
public enum IndexPeriod {

    DAILY,

    WEEKLY,

    MONTHLY,

    YEARLY,

    /**
     * Only one index should be created
     */
    ALL,

    /**
     * The index period is externally handled
     */
    EXTERNAL;

    public static Optional<LocalDate> getNextRollOverDate(IndexPeriod indexPeriod, LocalDate localDate) {
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        LocalDate rollOver = switch (indexPeriod) {
            case DAILY -> zonedDateTime.plusDays(1).toLocalDate();
            case WEEKLY -> zonedDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toLocalDate();
            case MONTHLY -> zonedDateTime.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).toLocalDate();
            case YEARLY -> zonedDateTime.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).toLocalDate();
            case ALL, EXTERNAL -> null;
        };
        return Optional.ofNullable(rollOver);
    }
}
