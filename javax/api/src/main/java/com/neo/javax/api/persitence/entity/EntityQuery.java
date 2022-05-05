package com.neo.javax.api.persitence.entity;

import com.neo.javax.api.persitence.criteria.SearchCriteria;

import java.util.*;

public class EntityQuery<T extends DataBaseEntity> {

    /**
     * The class of the DataBaseEntity
     */
    private Class<T> entityClass;

    /**
     * The fields that are returned in the result set
     */
    private List<String> fields;
    /**
     * The offset from where the result set starts
     */
    private int offset;
    /**
     * The max amount of results in the result set
     */
    private Integer maxResults;
    /**
     * The search criteria for the query
     */
    private List<SearchCriteria> filters;
    /**
     * How the fields should be sorted against
     */
    private Map<String, Boolean> sorting;

    public EntityQuery(Class<T> clazz, List<String> fields, int offset, Integer maxResults, List<SearchCriteria> filters,
            Map<String, Boolean> sorting) {
        this.entityClass = clazz;
        this.fields = fields;
        this.offset = offset;
        this.maxResults = maxResults;
        this.filters = filters;
        this.sorting = sorting;
    }

    public EntityQuery(Class<T> clazz, int offset, Integer maxResults, List<SearchCriteria> filters, Map<String, Boolean> sorting) {
        this(clazz, null, offset, maxResults, filters, sorting);
    }

    public EntityQuery(Class<T> clazz, Integer maxResults, List<SearchCriteria> filters) {
        this(clazz,0, maxResults, filters, new HashMap<>());
    }

    public EntityQuery(Class<T> clazz, List<SearchCriteria> filters) {
        this(clazz,null, filters);
    }

    public EntityQuery(Class<T> clazz) {
        this(clazz,new ArrayList<>());
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Optional<List<String>> getFields() {
        return Optional.ofNullable(fields);
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

    public Optional<Integer> getMaxResults() {
        return Optional.ofNullable(maxResults);
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
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
}