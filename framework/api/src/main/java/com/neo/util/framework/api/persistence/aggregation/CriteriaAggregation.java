package com.neo.util.framework.api.persistence.aggregation;

import com.neo.util.framework.api.persistence.criteria.SearchCriteria;

import java.util.List;
import java.util.Map;

public class CriteriaAggregation implements SearchAggregation {

    private String name;
    private Map<String, SearchCriteria> searchCriteriaMap;
    private SimpleFieldAggregation aggregation;

    public CriteriaAggregation(String name, Map<String, SearchCriteria> searchCriteriaMap, SimpleFieldAggregation aggregation) {
        this.name = name;
        this.searchCriteriaMap = searchCriteriaMap;
        this.aggregation = aggregation;
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<String, SearchCriteria> getSearchCriteriaMap() {
        return searchCriteriaMap;
    }

    public SimpleFieldAggregation getAggregation() {
        return aggregation;
    }

}


