package com.neo.util.framework.api.persistence.aggregation;

import com.neo.util.framework.api.persistence.criteria.SearchCriteria;

import java.util.Map;

/**
 * The {@link  CriteriaAggregation} executed a {@link  SimpleFieldAggregation} for each {@link  SearchCriteria}
 * in the {@link Map}
 */
public record CriteriaAggregation(String name, Map<String, SearchCriteria> searchCriteriaMap,
                                  SimpleFieldAggregation simpleFieldAggregation) implements SearchAggregation {

    @Override
    public String getName() {
        return name;
    }

    public Map<String, SearchCriteria> getSearchCriteriaMap() {
        return searchCriteriaMap;
    }

    public SimpleFieldAggregation getAggregation() {
        return simpleFieldAggregation;
    }

}


