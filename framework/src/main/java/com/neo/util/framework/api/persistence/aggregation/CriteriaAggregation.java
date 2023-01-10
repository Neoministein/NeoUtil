package com.neo.util.framework.api.persistence.aggregation;

import com.neo.util.framework.api.persistence.criteria.SearchCriteria;

import java.util.Map;
import java.util.Optional;

/**
 * The {@link  CriteriaAggregation} executed a {@link  SimpleFieldAggregation} for each {@link  SearchCriteria}
 * in the {@link Map}
 */
public record CriteriaAggregation(String name, Map<String, SearchCriteria> searchCriteriaMap,
                                  SimpleFieldAggregation simpleFieldAggregation) implements SearchAggregation {

    public CriteriaAggregation(String name, Map<String, SearchCriteria> searchCriteriaMap) {
        this(name, searchCriteriaMap, null);
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<String, SearchCriteria> getSearchCriteriaMap() {
        return searchCriteriaMap;
    }

    public Optional<SimpleFieldAggregation> getAggregation() {
        return Optional.ofNullable(simpleFieldAggregation);
    }

}


