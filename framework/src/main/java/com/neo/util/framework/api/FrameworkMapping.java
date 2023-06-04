package com.neo.util.framework.api;

import java.util.Optional;
import java.util.UUID;

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
}
