package com.neo.util.framework.api.persistence.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This criteria is a container for grouping criterion together. Default is the and operation.
 */
public class CombinedSearchCriteria implements SearchCriteria {

    protected final List<SearchCriteria> searchCriteriaList;
    protected final Association association;

    public enum Association {
        AND, OR
    }

    public CombinedSearchCriteria(SearchCriteria... searchCriterion) {
        this(Association.AND, searchCriterion);
    }

    public CombinedSearchCriteria(Association association, SearchCriteria... searchCriterion) {
        this.searchCriteriaList = Arrays.asList(searchCriterion);
        this.association = association;
    }

    public CombinedSearchCriteria(Association association) {
        searchCriteriaList = new ArrayList<>();
        this.association = association;
    }

    public CombinedSearchCriteria addCriteria(SearchCriteria searchCriteria) {
        searchCriteriaList.add(searchCriteria);
        return this;
    }

    public boolean isAnd() {
        return Association.AND.equals(association);
    }

    public Association getAssociation() {
        return association;
    }

    public List<SearchCriteria> getSearchCriteriaList() {
        return searchCriteriaList;
    }

}
