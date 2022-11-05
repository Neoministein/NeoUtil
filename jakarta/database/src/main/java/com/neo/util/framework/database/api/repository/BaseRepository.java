package com.neo.util.framework.database.api.repository;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This class provides the base functionality for Database access per {@link PersistenceEntity} which can be extended
 * upon with queries
 * @param <T> the {@link PersistenceEntity} of this repository
 */
public interface BaseRepository<T extends PersistenceEntity> {

    /**
     * Persists {@link T}
     *
     * @param entity the entity to be created
     */
    void create(T entity);

    /**
     * Persists the collection of {@link T}
     *
     * @param entities the entities to be created
     */
    void create(Collection<T> entities);


    /**
     * Edits {@link T}
     *
     * @param entity the entity to be edited
     */
    void edit(T entity);

    /**
     * Edits the collection of {@link T}
     *
     * @param entities the entities to be edited
     */
    void edit(Collection<T> entities);

    /**
     * Removes the entry in the table for the given {@link PersistenceEntity}
     *
     * @param entity the entity to remove
     */
    void remove(T entity);

    /**
     * Removes the collection of {@link T}
     *
     * @param entities the entities to remove
     */
    void remove(Collection<T> entities);

    /**
     * Finds{@link T} and returns it as an {@link Optional<T>}
     *
     * @param primaryKey the primary key of the searched object
     *
     * @return the entry as an {@link Optional<T>}
     */
    Optional<T> fetch(Object primaryKey);

    /**
     * Finds all {@link T} and returns them order asc by the provided column.
     *
     * @param columnOrder to order against
     *
     * @return all {@link T} order
     */
    List<T> findAll(String... columnOrder);

    /**
     * Finds all {@link T} which have the column match the value.
     * <p>
     * This method should only be used for columns with the unique attribute
     *
     * @param column to match against
     * @param value value to match
     *
     * @return all {@link T} which match the value in the column
     */
    Optional<T> fetchByValue(String column, String value);

    /**
     * Finds all {@link T} which have the column match the value returns them order asc by the provided column.
     *
     * @param column to match against
     * @param value value to match
     * @param columnOrder to order against
     *
     * @return all {@link T} which match the value in the column
     */
    List<T> fetchAllByValue(String column, String value, String... columnOrder);

    /**
     * Returns the number of {@link T} which are stored in the databse
     */
    long count();

}
