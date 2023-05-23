package com.neo.util.framework.api.persistence.search;

/**
 * The retention period for an index. This will be used to decided after how much time an index will be deleted.
 */
public enum RetentionPeriod {

    /**
     * The retention in calculated based on the {@link IndexPeriod}
     */
    INDEX_BASED,

    /**
     * The index retention is externally handled
     */
    EXTERNAL,

    /**
     * The default retention period which is defines in {@link #getDefault()}
     */
    DEFAULT;

    /**
     * The default retention period
     *
     * @return returns the default retention period
     */
    public static RetentionPeriod getDefault() {
        // Don't return default
        return EXTERNAL;
    }
}