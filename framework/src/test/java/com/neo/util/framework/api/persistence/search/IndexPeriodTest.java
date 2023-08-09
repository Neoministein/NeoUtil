package com.neo.util.framework.api.persistence.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class IndexPeriodTest {

    protected static final DateTimeFormatter DATE_FORMAT_DAY = DateTimeFormatter.ofPattern("yyyy.MM.ww.DDD")
            .withZone(ZoneId.systemDefault());

    @Test
    void testRollOver() {
        LocalDate dateToTest = LocalDate.parse("2023.05.20.139", DATE_FORMAT_DAY);

        LocalDate daily = IndexPeriod.getNextRollOverDate(IndexPeriod.DAILY, dateToTest).orElseThrow();
        LocalDate weekly = IndexPeriod.getNextRollOverDate(IndexPeriod.WEEKLY, dateToTest).orElseThrow();
        LocalDate monthly = IndexPeriod.getNextRollOverDate(IndexPeriod.MONTHLY, dateToTest).orElseThrow();
        LocalDate yearly = IndexPeriod.getNextRollOverDate(IndexPeriod.YEARLY, dateToTest).orElseThrow();
        Optional<LocalDate> all = IndexPeriod.getNextRollOverDate(IndexPeriod.ALL, dateToTest);
        Optional<LocalDate> external = IndexPeriod.getNextRollOverDate(IndexPeriod.EXTERNAL, dateToTest);


        Assertions.assertEquals(LocalDate.of(2023, 5, 20), daily);
        Assertions.assertEquals(LocalDate.of(2023, 5, 22), weekly);
        Assertions.assertEquals(LocalDate.of(2023, 6, 1), monthly);
        Assertions.assertEquals(LocalDate.of(2024, 1, 1), yearly);
        Assertions.assertTrue(all.isEmpty());
        Assertions.assertTrue(external.isEmpty());
    }
}