package com.neo.javax.api.persistence.search;

import com.neo.javax.api.persitence.search.Searchable;

/**
 * This service retrieves the index which shall be used in for the given searchable
 */
public interface IndexNamingService {

    /**
     * The index name for the searchable
     */
    String getIndexName(Searchable searchable);

    /**
     * The index name prefix for the class
     */
    String getIndexNamePrefixFromClass(Class<?> searchableClazz, boolean appendProjectIdPart);

    /**
     * Since multiple project could be housed in the same Cluster it's the project indices are separated
     * by a prefix. This is used to differentiate between different project.
     */
    String getIndexPrefix();

    /**
     * Since multiple project could be housed in the same Cluster it's the project indices are separated
     * by a postfix. Normally used for a project with multiple location
     */
    String getIndexPostfix();
}