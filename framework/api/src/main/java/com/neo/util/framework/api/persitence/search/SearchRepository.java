package com.neo.util.framework.api.persitence.search;

import java.util.List;

/**
 * This interfaces defines the necessary functionality of a SearchProvider service to store searchable objects in a persistent manner.
 */
public interface SearchRepository {

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

    SearchResult fetch(String index, SearchQuery parameters);

    /**
     * Reloads the config and reconnects to the SearchRepository to the nodes
     */
    void reload();

    boolean enabled();
}
