package com.neo.util.framework.api;

import jakarta.interceptor.Interceptor;

/**
 * <a href="https://docs.oracle.com/javaee/7/api/javax/interceptor/Interceptor.Priority.html">https://docs.oracle.com/javaee/7/api/javax/interceptor/Interceptor.Priority.html</a>
 */
public final class PriorityConstants {

    /**
     * Start of range for early interceptors defined by platform specifications.
     */
    public static final int PLATFORM_BEFORE = Interceptor.Priority.PLATFORM_BEFORE;

    /**
     * Start of range for early interceptors defined by extension libraries.
     */
    public static final int LIBRARY_BEFORE = Interceptor.Priority.LIBRARY_BEFORE;

    /**
     * Start of range for interceptors defined by applications.
     */
    public static final int APPLICATION =  Interceptor.Priority.APPLICATION;

    /**
     * Start of range for late interceptors defined by extension libraries.
     */
    public static final int LIBRARY_AFTER = Interceptor.Priority.LIBRARY_AFTER;

    /**
     * Start of range for late interceptors defined by platform specifications.
     */
    public static final int PLATFORM_AFTER = Interceptor.Priority.PLATFORM_AFTER;

    /**
     * Start of range for late interceptors defined by testing specifications.
     */
    public static final int TEST = 5000;

    private PriorityConstants() {}
}
