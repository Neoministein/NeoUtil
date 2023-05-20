package com.neo.util.framework.api.persistence.search;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableIndex {

    /**
     * The index name in which the searchable should be stored
     */
    String indexName();


    /**
     * The index period
     */
    IndexPeriod indexPeriod() default IndexPeriod.DEFAULT;

    /**
     * Retention Period of the index
     */
    RetentionPeriod retentionPeriod() default RetentionPeriod.DEFAULT;
}
