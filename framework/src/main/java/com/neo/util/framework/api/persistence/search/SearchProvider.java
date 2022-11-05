package com.neo.util.framework.api.persistence.search;

import java.util.List;

/**
 * This interfaces defines the interactions capability for search data storage {@link Searchable}
 * <p>
 * It provides a programmatic configurable layer between high level usage and the impl.
 */
public interface SearchProvider {

    void index(Searchable searchable);

    void index(Searchable searchable, IndexParameter indexParameter);

    void index(List<? extends Searchable> searchableList);

    void index(List<? extends Searchable> searchableList, IndexParameter indexParameter);

    void update(Searchable searchable, boolean partial);

    void update(Searchable searchable, boolean partial, IndexParameter indexParameter);

    void update(List<? extends Searchable> searchableList, boolean partial);

    void delete(Searchable searchable);

    void delete(List<? extends Searchable> searchableList);

    void deleteAll(Class<? extends Searchable> searchableClazz);

    void process(QueueableSearchable transportSearchable);

    void process(List<QueueableSearchable> transportSearchableList);

    SearchResult fetch(String index, SearchQuery parameters);

    /**
     * Reloads the config and reconnects to the SearchRepository to the nodes
     */
    void reload();

    boolean enabled();
}
