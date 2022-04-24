package com.neo.javax.api.persitence;

/**
 * The period for an index. This will be used to decided when new indexes have to be created.
 * Do not change a IndexPeriod for an entity on a running system.
 */
public enum IndexPeriod {

    DAILY,

    WEEKLY,

    MONTHLY,

    YEARLY,

    /**
     * Only one index should be created
     */
    ALL,

    /**
     * The index period is externally handled
     */
    EXTERNAL,

    /**
     * The default wich is defines in the function below
     */
    DEFAULT;

    /**
     * The default index period
     *
     * @return returns the default index period
     */
    public static IndexPeriod getDefault() {
        // Don't return default
        return YEARLY;
    }

}
