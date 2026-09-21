package com.neo.util.api.persistence.entity;

import java.util.Optional;

/**
 * This interfaces defines the interactions capability for persistence relational data storage per {@link EntityProvider}
 * <p>
 * It provides a programmatic configurable layer between high level usage and the impl.
 */
public interface EntityProvider {

    /**
     * Persists the {@link PersistenceEntity}
     *
     * @param entity the entity to be created
     */
    void create(PersistenceEntity<?> entity);

    /**
     * Edits the {@link PersistenceEntity}
     *
     * @param entity the entity to be edited
     */
    void edit(PersistenceEntity<?> entity);

    /**
     * Removes the {@link PersistenceEntity}
     *
     * @param entity the entity to remove
     */
    void remove(PersistenceEntity<?> entity);

    /**
     * Finds the {@link PersistenceEntity} and returns it as an {@link Optional<T>}
     *
     * @param primaryKey the primary key of the searched object
     *
     * @return the entry as an {@link Optional<T>}
     */
    <P, T extends PersistenceEntity<P>>  Optional<T> fetch(P primaryKey, Class<T> entityClazz);

    /**
     * Finds all {@link PersistenceEntity} which match the given parameter
     *
     * @param parameters the parameters
     * @param <X> the entity type
     *
     * @return the result of the given search
     */
    <X extends PersistenceEntity<?>> EntityResult<X> fetch(EntityQuery<X> parameters);
}
