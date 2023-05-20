package com.neo.util.framework.api.persistence.search;

import java.time.*;
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
    EXTERNAL,

    /**
     * The default index period which is defines in {@link #getDefault()}
     */
    DEFAULT;

    /**
     * The default index period
     *
     * @return returns the default index period
     */
    public static IndexPeriod getDefault() {
        // Don't return default
        return YEARLY;
    }

    public static Optional<LocalDate> getNextRollOverDate(IndexPeriod indexPeriod, LocalDate localDate) {
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        LocalDate rollOver = switch (indexPeriod) {
            case DAILY -> zonedDateTime.plusDays(1).toLocalDate();
            case WEEKLY -> zonedDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toLocalDate();
            case MONTHLY -> zonedDateTime.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).toLocalDate();
            case YEARLY -> zonedDateTime.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).toLocalDate();
            case ALL, EXTERNAL -> null;
            case DEFAULT -> getNextRollOverDate(IndexPeriod.getDefault(), localDate).orElse(null);
        };
        return Optional.ofNullable(rollOver);
    }
}
