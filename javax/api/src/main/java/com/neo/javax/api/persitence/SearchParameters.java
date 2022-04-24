package com.neo.javax.api.persitence;

import com.neo.javax.api.persitence.aggregation.SearchAggregation;
import com.neo.javax.api.persitence.criteria.SearchCriteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a search query for a search provider
 */
public class SearchParameters implements Serializable {

    private static final int SEARCH_PARAMETERS_DEFAULT = 10;

    /**
     * Defines if the search should be executed on the current index based on the {@link IndexPeriod} or should take all
     * indices into account
     */
    public enum SearchPeriod {
        CURRENT, ALL
    }

    /**
     * The fields that are returned in the result set
     */
    private List<String> fields;
    /**
     *  The offset from where the result set starts
     */
    private int offset;
    /**
     * The max amount of results in the result set
     */
    private Integer maxResults;
    /**
     * The max duration in ms the search query can take
     * */
    private Long timeout;
    /**
     * The search criteria for the query
     */
    private List<SearchCriteria> filters;
    /**
     * How the fields should be sorted against
     */
    private Map<String, Boolean> sorting;
    /**
     * The list of aggregations
     * */
    private List<SearchAggregation> aggregations;
    /**
     * The search period, default = ALL
     */
    private SearchPeriod searchPeriod = SearchPeriod.ALL;

    /**
     * Create a new SearchParameters.
     */
    public SearchParameters(List<String> fields, int offset, Integer maxResults, Long timeout, List<SearchCriteria> filters,
            Map<String, Boolean> sorting) {
        super();
        this.fields = fields;
        this.offset = offset;
        this.maxResults = maxResults;
        this.timeout = timeout;
        this.filters = filters;
        this.sorting = sorting;
    }

    public SearchParameters(List<String> fields, int offset ,Integer maxResults, List<SearchCriteria> filters) {
        this(fields, offset, maxResults, null, filters, new HashMap<>());
    }

    public SearchParameters(Integer maxResults, List<SearchCriteria> filters) {
        this(null,0 ,maxResults, filters);
    }

    public SearchParameters() {
        this(SEARCH_PARAMETERS_DEFAULT);
    }

    public SearchParameters(Integer maxResults) {
        this(null,0 ,maxResults, null, new ArrayList<>(), new HashMap<>());
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public List<SearchCriteria> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchCriteria> filters) {
        this.filters = filters;
    }

    public Map<String, Boolean> getSorting() {
        return sorting;
    }

    public void setSorting(Map<String, Boolean> sorting) {
        this.sorting = sorting;
    }

    public List<SearchAggregation> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<SearchAggregation> aggregations) {
        this.aggregations = aggregations;
    }

    public SearchPeriod getSearchPeriod() {
        return searchPeriod;
    }

    public void setSearchPeriod(SearchPeriod searchPeriod) {
        this.searchPeriod = searchPeriod;
    }
}