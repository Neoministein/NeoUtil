package com.neo.javax.api.persitence.entity;

import javax.transaction.RollbackException;
import java.util.Optional;

/**
 * This interfaces defines the interactions capability for persistence relational data storage per database entity
 */
public interface EntityRepository {

    /**
     * Creates a entry in the table for the given {@link DataBaseEntity}
     *
     * @param entity the entity to be created
     */
    void create(DataBaseEntity entity) throws RollbackException;

    /**
     * Edits the given entry in the table for the given {@link DataBaseEntity}
     *
     * @param entity the entity to be edited
     */
    void edit(DataBaseEntity entity) throws RollbackException;

    /**
     * Removes the entry in the table for the given {@link DataBaseEntity}
     *
     * @param entity the entity to remove
     */
    void remove(DataBaseEntity entity) throws RollbackException;

    /**
     * Finds the entry in the table and returns it as an {@link Optional<X>}
     *
     * @param primaryKey the primary key of the searched object
     *
     * @return the entry as an {@link Optional<X>}
     */
    <X extends DataBaseEntity> Optional<X> find(Object primaryKey, Class<X> entityClazz);

    /**
     * Finds all entities which match the given parameter
     *
     * @param parameters the parameters
     * @param <X> the entity type
     *
     * @return the result of the given search
     */
    <X extends DataBaseEntity> EntityResult<X> find(EntityQuery<X> parameters);
}
