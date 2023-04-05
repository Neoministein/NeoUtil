package com.neo.util.framework.api.persistence.search;

import com.neo.util.framework.api.persistence.criteria.SearchCriteria;

import java.util.Collection;

/**
 * This interfaces defines the interactions capability for search data storage {@link Searchable}
 * <p>
 * It provides a programmatic configurable layer between high level usage and the impl.
 */
public interface SearchProvider {

    void index(Searchable searchable);

    void index(Searchable searchable, IndexParameter indexParameter);

    void index(Collection<? extends Searchable> searchableList);

    void index(Collection<? extends Searchable> searchableList, IndexParameter indexParameter);

    void update(Searchable searchable, boolean upsert);

    void update(Searchable searchable, boolean upsert, IndexParameter indexParameter);

    void update(Collection<? extends Searchable> searchableList, boolean upsert);

    void update(Collection<? extends Searchable> searchableList, boolean upsert, IndexParameter indexParameter);

    void delete(Searchable searchable);

    void delete(Collection<? extends Searchable> searchableList);

    void deleteAll(Class<? extends Searchable> searchableClazz);

    void process(QueueableSearchable transportSearchable);

    void process(Collection<QueueableSearchable> transportSearchableList);

    long count(Class<? extends Searchable> searchableClazz);

    long count(Class<? extends Searchable> searchableClazz, Collection<SearchCriteria> searchCriteriaList);

    <T> SearchResult<T> fetch(String index, SearchQuery parameters, Class<T> hitsClass);

    /**
     * Reloads the config and reconnects to the SearchRepository to the nodes
     */
    void reload();

    boolean enabled();
}
