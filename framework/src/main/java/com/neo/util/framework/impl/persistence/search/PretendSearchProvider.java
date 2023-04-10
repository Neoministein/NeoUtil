package com.neo.util.framework.impl.persistence.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.framework.api.persistence.criteria.SearchCriteria;
import com.neo.util.framework.api.persistence.search.*;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;

@ApplicationScoped
@SuppressWarnings("java:S1186") //Default search implementation that does nothing
public class PretendSearchProvider implements SearchProvider {

    @Override
    public void index(Searchable searchable) {

    }

    @Override
    public void index(Searchable searchable, IndexParameter indexParameter) {

    }

    @Override
    public void index(Collection<? extends Searchable> searchableList) {

    }

    @Override
    public void index(Collection<? extends Searchable> searchableList, IndexParameter indexParameter) {

    }

    @Override
    public void update(Searchable searchable, boolean partial) {

    }

    @Override
    public void update(Searchable searchable, boolean partial, IndexParameter indexParameter) {

    }

    @Override
    public void update(Collection<? extends Searchable> searchableList, boolean partial) {

    }

    @Override
    public void update(Collection<? extends Searchable> searchableList, boolean partial, IndexParameter indexParameter) {

    }

    @Override
    public void delete(Searchable searchable) {

    }

    @Override
    public void delete(Collection<? extends Searchable> searchableList) {

    }

    @Override
    public void deleteAll(Class<? extends Searchable> searchableClazz) {

    }

    @Override
    public void process(QueueableSearchable transportSearchable) {

    }

    @Override
    public void process(Collection<QueueableSearchable> transportSearchableList) {

    }

    @Override
    public long count(Class<? extends Searchable> searchableClazz) {
        return 0;
    }

    @Override
    public long count(Class<? extends Searchable> searchableClazz, Collection<SearchCriteria> searchCriteriaList) {
        return 0;
    }

    @Override
    public SearchResult<JsonNode> fetch(String index, SearchQuery parameters) {
        return null;
    }

    @Override
    public <T> SearchResult<T> fetch(String index, SearchQuery parameters, Class<T> hitsClass) {
        return null;
    }

    @Override
    public void reload() {

    }

    @Override
    public boolean enabled() {
        return false;
    }
}
