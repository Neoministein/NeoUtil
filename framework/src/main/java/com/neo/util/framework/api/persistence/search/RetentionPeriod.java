package com.neo.util.framework.api.persistence.search;

public enum RetentionPeriod {

    INDEX_PERIOD_BASED,

    /**
     * The default retention period which is defines in {@link #getDefault()}
     */
    DEFAULT;

    /**
     * The default index period
     *
     * @return returns the default index period
     */
    public static RetentionPeriod getDefault() {
        // Don't return default
        return INDEX_PERIOD_BASED;
    }
}
