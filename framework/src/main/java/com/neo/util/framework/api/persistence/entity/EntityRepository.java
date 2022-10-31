package com.neo.util.framework.api.persistence.entity;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.RollbackException;
import java.util.Optional;

/**
 * This interfaces defines the interactions capability for persistence relational data storage per database entity
 */
public interface EntityRepository {

    /**
     * Creates a entry in the table for the given {@link PersistenceEntity}
     *
     * @param entity the entity to be created
     * @throws RollbackException if unique is violated
     * @throws PersistenceException if nullable is violated
     */
    void create(PersistenceEntity entity);

    /**
     * Edits the given entry in the table for the given {@link PersistenceEntity}
     *
     * @param entity the entity to be edited
     * @throws RollbackException if unique is violated
     * @throws PersistenceException if nullable is violated
     */
    void edit(PersistenceEntity entity);

    /**
     * Removes the entry in the table for the given {@link PersistenceEntity}
     *
     * @param entity the entity to remove
     * @throws RollbackException if unique is violated
     * @throws PersistenceException if nullable is violated
     */
    void remove(PersistenceEntity entity);

    /**
     * Finds the entry in the table and returns it as an {@link Optional<X>}
     *
     * @param primaryKey the primary key of the searched object
     *
     * @return the entry as an {@link Optional<X>}
     */
    <X extends PersistenceEntity> Optional<X> find(Object primaryKey, Class<X> entityClazz);

    /**
     * Finds all entities which match the given parameter
     *
     * @param parameters the parameters
     * @param <X> the entity type
     *
     * @return the result of the given search
     */
    <X extends PersistenceEntity> EntityResult<X> find(EntityQuery<X> parameters);
}
