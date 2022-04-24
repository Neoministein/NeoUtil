package com.neo.javax.api.persitence.repository;

import com.neo.javax.api.persitence.IndexParameter;
import com.neo.javax.api.persitence.SearchParameters;
import com.neo.javax.api.persitence.SearchResult;
import com.neo.javax.api.persitence.entity.Searchable;

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

    SearchResult fetch(String index, SearchParameters parameters);

    void reconnect();
}
