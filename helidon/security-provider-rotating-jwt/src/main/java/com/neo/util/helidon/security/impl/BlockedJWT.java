package com.neo.util.helidon.security.impl;

public record BlockedJWT(String uid, long invalidUntil) {

}
