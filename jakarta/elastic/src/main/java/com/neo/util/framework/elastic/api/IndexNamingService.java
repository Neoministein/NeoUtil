package com.neo.util.framework.elastic.api;

import com.neo.util.framework.api.persistence.search.Searchable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

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
    String getIndexNamePrefixFromClass(Class<? extends Searchable> searchableClazz, boolean appendProjectIdPart);

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

    /**
     * Returns all existing searchables
     */
    Set<Class<? extends Searchable>> getAllSearchables();

    /**
     * Parses the date from the index name
     *
     * @param searchableClazz the searchable for the index
     * @param indexName the index name
     *
     * @return {@link Optional} of {@link LocalDate} since not all index names store the date inside them
     */
    Optional<LocalDate> getDateFromIndexName(Class<? extends Searchable> searchableClazz, String indexName);
}