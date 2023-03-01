package com.neo.util.framework.api.persistence.search;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the index for the class
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface SearchableIndex {

    /**
     * The index name in which the searchable should be stored
     */
    String index();

    /**
     * The period for an index. This will be used to decided when new indexes have to be created.
     */
    IndexPeriod indexPeriod() default IndexPeriod.DEFAULT;

    RetentionPeriod retentionPeriod() default RetentionPeriod.DEFAULT;
}
