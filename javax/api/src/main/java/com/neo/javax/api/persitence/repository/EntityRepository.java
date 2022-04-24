package com.neo.javax.api.persitence.repository;

import com.neo.javax.api.persitence.criteria.SearchCriteria;
import com.neo.javax.api.persitence.entity.DataBaseEntity;

import javax.transaction.RollbackException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This interfaces defines the interactions capability for persistence relational data storage per database entity
 */
public interface EntityRepository<T extends DataBaseEntity> {

    /**
     * Creates a entry in the table for the given entity {@link T}
     *
     * @param entity the entity to be created
     */
    void create(T entity) throws RollbackException;

    /**
     * Edits the given entry in the table for the given entity {@link T}
     *
     * @param entity the entity to be edited
     */
    void edit(T entity) throws RollbackException;

    /**
     * Removes the entry in the table for the given entity {@link T}
     *
     * @param entity the entity to remove
     */
    void remove(T entity) throws RollbackException;

    /**
     * Counts the number of entries in the table {@link T}
     *
     * @return the number of entries
     */
    int count();

    /**
     * Counts the number of entries in the table {@link T} which match the provided filters
     *
     * @param filters the filters to search against
     *
     * @return the number of entries
     */
    int count(List<? extends SearchCriteria> filters);

    /**
     * Finds the entry in the table and returns it as an {@link Optional<T>}
     *
     * @param primaryKey the primary key of the searched object
     *
     * @return the entry as an {@link Optional<T>}
     */
    Optional<T> find(Object primaryKey);

    /**
     * Finds all entries in the table and returns is as the a {@link List<T>}
     *
     * @return all entities
     */
    List<T> findAll();

    /**
     * Finds a single entry in the table based on the filters and returns is as an {@link Optional<T>}
     *
     * @param filters the filters to search against
     *
     * @return the entity as an {@link Optional<T>}
     */
    Optional<T> find(List<? extends SearchCriteria> filters);

    /**
     * Finds all entries which match the filters
     *
     * @param filters the filters to search against
     * @param sorting the sort order
     * @param offset the offset from the first
     *
     * @return a list entities
     */
    List<T> find(List<? extends SearchCriteria> filters, Map<String, Boolean> sorting, int offset);

    /**
     * Finds all entries which match the filters
     *
     * @param filters the filters to search against
     * @param sorting the sort order
     * @param offset the offset from the first
     * @param maxReturn the max amount of entries that should be returned
     *
     * @return a list entities
     */
    List<T> find(List<? extends SearchCriteria> filters, Map<String, Boolean> sorting, int offset, int maxReturn);
}
