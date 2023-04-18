package com.neo.util.framework.api.persistence.search;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableIndex {

    String INDEX_NAME = "indexName";
    String INDEX_PERIOD = "indexPeriod";


    /**
     * The index name in which the searchable should be stored
     */
    String indexName();


    /**
     * The index period
     */
    IndexPeriod indexPeriod() default IndexPeriod.DEFAULT;
}
