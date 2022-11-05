package com.neo.util.framework.impl.persistence.search;

import com.neo.util.framework.api.persistence.search.*;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

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
    public void index(List<? extends Searchable> searchableList) {

    }

    @Override
    public void index(List<? extends Searchable> searchableList, IndexParameter indexParameter) {

    }

    @Override
    public void update(Searchable searchable, boolean partial) {

    }

    @Override
    public void update(Searchable searchable, boolean partial, IndexParameter indexParameter) {

    }

    @Override
    public void update(List<? extends Searchable> searchableList, boolean partial) {

    }

    @Override
    public void delete(Searchable searchable) {

    }

    @Override
    public void delete(List<? extends Searchable> searchableList) {

    }

    @Override
    public void deleteAll(Class<? extends Searchable> searchableClazz) {

    }

    @Override
    public void process(QueueableSearchable transportSearchable) {

    }

    @Override
    public void process(List<QueueableSearchable> transportSearchableList) {

    }

    @Override
    public SearchResult fetch(String index, SearchQuery parameters) {
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
