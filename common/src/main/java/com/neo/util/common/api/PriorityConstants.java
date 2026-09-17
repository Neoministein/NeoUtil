package com.neo.util.common.api;

/**
 * <a href="https://docs.oracle.com/javaee/7/api/javax/interceptor/Interceptor.Priority.html">https://docs.oracle.com/javaee/7/api/javax/interceptor/Interceptor.Priority.html</a>
 */
public final class PriorityConstants {

    /**
     * Start of range for early interceptors defined by platform specifications.
     */
    public static final int PLATFORM_BEFORE = 0;

    /**
     * Start of range for early interceptors defined by extension libraries.
     */
    public static final int LIBRARY_BEFORE = 1000;

    /**
     * Start of range for interceptors defined by applications.
     */
    public static final int APPLICATION =  2000;

    /**
     * Start of range for late interceptors defined by extension libraries.
     */
    public static final int LIBRARY_AFTER = 3000;

    /**
     * Start of range for late interceptors defined by platform specifications.
     */
    public static final int PLATFORM_AFTER = 4000;

    /**
     * Start of range for late interceptors defined by testing specifications.
     */
    public static final int TEST = 5000;

    private PriorityConstants() {}
}
