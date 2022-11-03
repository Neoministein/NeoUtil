package com.neo.util.framework.api.persistence.entity;

import java.util.Optional;

/**
 * This interfaces defines the interactions capability for persistence relational data storage per database entity
 */
public interface EntityRepository {

    /**
     * Persists the {@link PersistenceEntity}
     *
     * @param entity the entity to be created
     */
    void create(PersistenceEntity entity);

    /**
     * Edits the {@link PersistenceEntity}
     *
     * @param entity the entity to be edited
     */
    void edit(PersistenceEntity entity);

    /**
     * Removes the {@link PersistenceEntity}
     *
     * @param entity the entity to remove
     */
    void remove(PersistenceEntity entity);

    /**
     * Finds the {@link PersistenceEntity} and returns it as an {@link Optional<X>}
     *
     * @param primaryKey the primary key of the searched object
     *
     * @return the entry as an {@link Optional<X>}
     */
    <X extends PersistenceEntity> Optional<X> find(Object primaryKey, Class<X> entityClazz);

    /**
     * Finds all {@link PersistenceEntity} which match the given parameter
     *
     * @param parameters the parameters
     * @param <X> the entity type
     *
     * @return the result of the given search
     */
    <X extends PersistenceEntity> EntityResult<X> find(EntityQuery<X> parameters);
}
