package com.neo.util.helidon.security.impl;

public class BlockedJWT {

    private final String uid;
    private final long invalidUntil;

    public BlockedJWT(String uid, long invalidUntil) {
        this.uid = uid;
        this.invalidUntil = invalidUntil;
    }

    public String getUid() {
        return uid;
    }

    public long getInvalidUntil() {
        return invalidUntil;
    }
}
