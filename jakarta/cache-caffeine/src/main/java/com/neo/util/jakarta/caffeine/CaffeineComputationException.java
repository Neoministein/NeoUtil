package com.neo.util.jakarta.caffeine;

/**
 * This class is used to prevent Caffeine from logging unwanted warnings.
 */
public record CaffeineComputationException(Throwable cause) {

}
