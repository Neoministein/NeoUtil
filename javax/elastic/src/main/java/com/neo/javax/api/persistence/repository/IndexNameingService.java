package com.neo.javax.api.persistence.repository;

import com.neo.javax.api.persitence.entity.Searchable;

/**
 * This service retrieves the index which shall be used in for the given searchable
 */
public interface IndexNameingService {

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
     * by the defined id
     */
    String getProjectIdPostfix();
}