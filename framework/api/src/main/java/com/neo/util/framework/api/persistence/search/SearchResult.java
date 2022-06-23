package com.neo.util.framework.api.persistence.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.api.json.Views;
import com.neo.util.framework.api.persistence.aggregation.AggregationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a result from a search query from a search provider
 */
public class SearchResult {

    /**
     * Total number of hits which match the query, regardless of the maxResults.
     */
        @JsonIgnore
    private long hitSize;
    /**
     * Maximum score of the found search results
     */
        @JsonIgnore
    private double maxScore;
    /**
     * Duration of search in milliseconds
     */
        @JsonView(Views.Owner.class)
    private long tookInMillis;
    /**
     * true in case the search was terminated due to reaching the timeout setting in the search parameter
     */
        @JsonView(Views.Owner.class)
    private boolean terminatedEarly;
    /**
     * true in case the search was terminated by elasticsearch's timeout settings
     */
        @JsonView(Views.Owner.class)
    private boolean timedOut;
    /**
     * scrollId in case paging is used
     */
        @JsonView(Views.Public.class)
    private String scrollId;

    /**
     * true in case the search returned the maximum number of records, potentially more records exist
     */
        @JsonView(Views.Public.class)
    private boolean relationGreaterThenEqualTo;

    /**
     * The search hits found
     */
        @JsonView(Views.Public.class)
    private List<JsonNode> hits;
    /**
     * The aggregations defined in the SearchParameter's
     */
        @JsonView(Views.Public.class)
    private Map<String, AggregationResult> aggregations = new HashMap<>();

    /**
     * Create a new SearchResult.
     */
    public SearchResult(long hitSize, double maxScore, long tookInMillis, boolean terminatedEarly, boolean timedOut,
            String scrollId, List<JsonNode> hits, Map<String, AggregationResult> aggregations,
            boolean relationGreaterThen) {
        super();
        this.hitSize = hitSize;
        this.maxScore = maxScore;
        this.tookInMillis = tookInMillis;
        this.terminatedEarly = terminatedEarly;
        this.timedOut = timedOut;
        this.scrollId = scrollId;
        this.hits = hits;
        this.aggregations = aggregations;
        this.relationGreaterThenEqualTo = relationGreaterThen;
    }

    /**
     * Empty SearchResult
     */
    public SearchResult() {
        hitSize = 0;
        maxScore = 0;
        tookInMillis = 0;
        terminatedEarly = false;
        timedOut = false;
        hits = new ArrayList<>();
    }

    public long getHitSize() {
        return hitSize;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public long getTookInMillis() {
        return tookInMillis;
    }

    public boolean isTerminatedEarly() {
        return terminatedEarly;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public String getScrollId() {
        return scrollId;
    }

    public List<JsonNode> getHits() {
        return hits;
    }

    public Map<String, AggregationResult> getAggregations() {
        return aggregations;
    }

    public void setAggregations(Map<String, AggregationResult> aggregations) {
        this.aggregations = aggregations;
    }

    /**
     * @return the relationGreaterThenEqualTo
     */
    public boolean isRelationGreaterThenEqualTo() {
        return relationGreaterThenEqualTo;
    }

    @Override
    public String toString() {
        return "SearchResult [hitSize=" + hitSize + ", tookInMillis=" + tookInMillis + ", returnedHits=" + hits.size()
                + "]";
    }
}