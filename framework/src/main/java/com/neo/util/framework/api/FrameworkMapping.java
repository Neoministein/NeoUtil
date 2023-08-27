package com.neo.util.framework.api;

import com.neo.util.common.impl.StringUtils;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class FrameworkMapping {

    public static final int UUID = 36;

    public static Optional<UUID> optionalUUID(String uuid) {
        if (uuid == null || uuid.length() != UUID) {
            return Optional.empty();
        }

        try {
            return Optional.of(java.util.UUID.fromString(uuid));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static <E extends Enum<E>> Optional<E> parseEnumFromString(Class<E> clazz, String value) {
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }

        value = value.trim();
        for (E type : EnumSet.allOf(clazz)) {
            if (type.name().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static <E extends Enum<E>> Optional<E> parseEnumFromString(Class<E> clazz, String value, boolean strict) {
        if (!strict) {
            return parseEnumFromString(clazz, value);
        }

        for (E type : EnumSet.allOf(clazz)) {
            if (type.name().equals(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> parseInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static Optional<Duration> parseDuration(String timeUnit, String time) {
        Optional<TimeUnit> optTimeUnit = parseEnumFromString(TimeUnit.class, timeUnit);
        Optional<Integer> optTime = parseInteger(time);

        if (optTimeUnit.isPresent() && optTime.isPresent()) {
            return Optional.ofNullable(Duration.ofSeconds(optTimeUnit.get().toSeconds(optTime.get())));
        }
        return Optional.empty();
    }
}
