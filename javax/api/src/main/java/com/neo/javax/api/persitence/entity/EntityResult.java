package com.neo.javax.api.persitence.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a result from a search query from a search provider
 */
public class EntityResult<T extends DataBaseEntity> {

    /**
     * Total number of hits which match the query, regardless of the maxResults.
     */
    private long hitSize;
    /**
     * Duration of search in milliseconds
     */
    private long tookInMillis;

    /**
     * The search hits found
     */
    private List<T> hits;

    public EntityResult(long hitSize, long tookInMillis,
            List<T> hits) {
        super();
        this.hitSize = hitSize;
        this.tookInMillis = tookInMillis;
        this.hits = hits;
    }

    public EntityResult() {
        hitSize = 0;
        tookInMillis = 0;
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
    }
}