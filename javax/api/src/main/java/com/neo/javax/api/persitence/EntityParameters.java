package com.neo.javax.api.persitence;

import com.neo.javax.api.persitence.criteria.SearchCriteria;
import com.neo.javax.api.persitence.entity.DataBaseEntity;

import java.util.*;

public class EntityParameters<T extends DataBaseEntity> {

    /**
     * The class of the DataBaseEntity
     */
    private Class<T> entityClass;

    /**
     * The fields that are returned in the result set
     */
    private Optional<List<String>> fields;
    /**
     * The offset from where the result set starts
     */
    private int offset;
    /**
     * The max amount of results in the result set
     */
    private Optional<Integer> maxResults;
    /**
     * The search criteria for the query
     */
    private List<SearchCriteria> filters;
    /**
     * How the fields should be sorted against
     */
    private Map<String, Boolean> sorting;

    public EntityParameters(Class<T> clazz, List<String> fields, int offset, Integer maxResults, List<SearchCriteria> filters,
            Map<String, Boolean> sorting) {
        this.entityClass = clazz;
        this.fields = Optional.ofNullable(fields);
        this.offset = offset;
        this.maxResults = Optional.ofNullable(maxResults);
        this.filters = filters;
        this.sorting = sorting;
    }

    public EntityParameters(Class<T> clazz, List<String> fields, int offset, Integer maxResults, List<SearchCriteria> filters) {
        this(clazz, fields, offset, maxResults, filters, new HashMap<>());
    }

    public EntityParameters(Class<T> clazz, Integer maxResults, List<SearchCriteria> filters) {
        this(clazz,null, 0, maxResults, filters);
    }

    public EntityParameters(Class<T> clazz, List<SearchCriteria> filters) {
        this(clazz,0, filters);
    }

    public EntityParameters(Class<T> clazz) {
        this(clazz,new ArrayList<>());
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Optional<List<String>> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            this.fields = Optional.empty();
        }
        this.fields = Optional.empty();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Optional<Integer> getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = Optional.ofNullable(maxResults);
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