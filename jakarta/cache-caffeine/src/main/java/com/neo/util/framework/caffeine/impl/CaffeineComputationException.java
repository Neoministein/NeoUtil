package com.neo.util.framework.caffeine.impl;

/**
 * This class is used to prevent Caffeine from logging unwanted warnings.
 */
public record CaffeineComputationException(Throwable cause) {

}
