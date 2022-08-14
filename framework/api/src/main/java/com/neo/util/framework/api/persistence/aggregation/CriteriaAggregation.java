package com.neo.util.framework.api.persistence.aggregation;

import com.neo.util.framework.api.persistence.criteria.SearchCriteria;

import java.util.List;

public class CriteriaAggregation implements SearchAggregation {

    private String name;
    private List<KeyedCriteria> searchCriteriaList;
    private SimpleFieldAggregation aggregation;

    public CriteriaAggregation(String name, List<KeyedCriteria> searchCriteriaList, SimpleFieldAggregation aggregation) {
        this.name = name;
        this.searchCriteriaList = searchCriteriaList;
        this.aggregation = aggregation;
    }

    @Override
    public String getName() {
        return name;
    }


    public List<KeyedCriteria> getSearchCriteriaList() {
        return searchCriteriaList;
    }

    public SimpleFieldAggregation getAggregation() {
        return aggregation;
    }

    public record KeyedCriteria(String key, SearchCriteria searchCriteria) {}
}


