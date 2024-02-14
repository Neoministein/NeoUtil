package com.neo.util.framework.rest.web.htmx;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@ApplicationScoped
public class ResourceFormattingService {

    private static final String UNKNOWN = "UNKNOWN";

    private static final DateTimeFormatter DATE_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm.ss")
            .withZone(ZoneId.systemDefault());

    public String toDateSecond(TemporalAccessor temporalAccessor) {
        return temporalAccessor != null ? DATE_SECOND_FORMATTER.format(temporalAccessor) : UNKNOWN;
    }
}
