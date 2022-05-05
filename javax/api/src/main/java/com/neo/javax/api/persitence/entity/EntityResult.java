package com.neo.javax.api.persitence.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.neo.common.api.json.Views;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a result from a search query from a search provider
 */
public class EntityResult<T extends DataBaseEntity> {

    /**
     * Total number of hits which match the query, regardless of the maxResults.
     */
        @JsonView(Views.Public.class)
    private long hitSize;

    /**
     * The max number of hits that will be returned
     */
        @JsonView(Views.Public.class)
    private long hitCount;

    /**
     * Duration of search in milliseconds
     */
        @JsonView(Views.Internal.class)
    private long tookInMillis;

    /**
     * The search hits found
     */
        @JsonView(Views.Public.class)
    private List<T> hits;

    public EntityResult(long hitSize, long tookInMillis, List<T> hits) {
        super();
        this.hitSize = hitSize;
        this.hitCount = hits.size();
        this.tookInMillis = tookInMillis;
        this.hits = hits;
    }

    public EntityResult() {
        hitSize = 0L;
        hitCount = 0L;
        tookInMillis = 0L;
        hits = new ArrayList<>();
    }

    public long getHitSize() {
        return hitSize;
    }

    public void setHitSize(long hitSize) {
        this.hitSize = hitSize;
    }

    public long getTookInMillis() {
        return tookInMillis;
    }

    public void setTookInMillis(long tookInMillis) {
        this.tookInMillis = tookInMillis;
    }

    public List<T> getHits() {
        return hits;
    }

    public void setHits(List<T> hits) {
        this.hits = hits;
        this.hitCount = hits.size();
    }

    public long getHitCount() {
        return hitCount;
    }
}