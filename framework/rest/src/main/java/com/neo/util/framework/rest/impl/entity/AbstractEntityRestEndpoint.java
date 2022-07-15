package com.neo.util.framework.rest.impl.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.persistence.criteria.ExplicitSearchCriteria;
import com.neo.util.framework.api.persistence.entity.DataBaseEntity;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityRepository;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * This abstract class provides the base CRUD functionality for {@link DataBaseEntity}
 *
 * @param <T> the {@link DataBaseEntity} to be accessed
 */
public abstract class AbstractEntityRestEndpoint<T extends DataBaseEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityRestEndpoint.class);

    protected ObjectNode errorNotFound;
    protected ObjectNode errorCannotParse;
    protected ObjectNode errorMissingFields;
    protected ObjectNode errorNotUnique;
    protected ObjectNode errorInvalidResourcePermission;

    protected static final String ENTITY_PERM = "CRUD_";

    protected static final String PERM_INTERNAL = "internal";

    @Inject
    protected EntityRepository entityRepository;

    @Inject
    protected RequestDetails requestDetails;

    @Inject
    protected ResponseGenerator responseGenerator;

    protected abstract Object convertToPrimaryKey(String primaryKey);

    protected abstract Class<T> getEntityClass();

    @PostConstruct
    protected void init() {
        errorNotFound = responseGenerator.errorObject("resources/000","Entity not found");
        errorCannotParse = responseGenerator.errorObject("resources/001","Unable to retrieve entity");
        errorMissingFields = responseGenerator.errorObject("resources/002","Entity is missing mandatory fields");
        errorNotUnique = responseGenerator.errorObject("resources/003","Provided value isn't unique");
        errorInvalidResourcePermission = responseGenerator.errorObject("resources/004","Invalid resource permission");
    }

    protected Response getByPrimaryKey(String primaryKey) {
        return entityByColumn(DataBaseEntity.C_ID, convertToPrimaryKey(primaryKey));
    }

    protected Response getByValue(String column, String value) {
        return entityByColumn(column, value);
    }

    protected Response create(String x) {
        T entity = JsonUtil.fromJson(x, getEntityClass(), getSerializationScope());
        try {
            entityRepository.create(entity);
            LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
        } catch (RollbackException ex) {
            LOGGER.debug("Provided value isn't unique");
            return responseGenerator.error(400, errorNotUnique);
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return responseGenerator.error(400, errorMissingFields);
        }
        return parseEntityToResponse(entity,Views.Owner.class);
    }

    protected Response edit(String x) {
        return editEntity(parseJSONIntoExistingEntity(x, getSerializationScope()), getSerializationScope());
    }

    protected Response delete(String primaryKey) {
        Optional<T> entity = entityRepository.find(convertToPrimaryKey(primaryKey), getEntityClass());

        if (entity.isEmpty()) {
            LOGGER.debug("Entity not found [{},{}]", getEntityClass().getSimpleName() ,primaryKey);
            return responseGenerator.error(404, errorNotFound);
        }

        try {
            entityRepository.remove(entity.get());
            LOGGER.info("Deleted entity [{},{}]",getEntityClass().getSimpleName(), entity.get().getPrimaryKey());
            return responseGenerator.success();
        } catch (RollbackException ex) {
            return responseGenerator.error(400, errorMissingFields);
        }
    }

    /**
     * Retrieves an entity by a column value association and generates a response
     *
     * @param field the entity field
     * @param value the value which entity field must have
     *
     * @return the response to be delivered to the client
     */
    protected Response entityByColumn(String field, Object value) {
        EntityQuery<T> entityParameters = new EntityQuery<>(getEntityClass(), 1, List.of(new ExplicitSearchCriteria(field, value)));
        EntityResult<T> entity = entityRepository.find(entityParameters);
        if (entity.getHitSize() == 1) {
            LOGGER.debug("Entity not found [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
            return responseGenerator.error(404, errorNotFound);
        }
        LOGGER.trace("Entity lookup success [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
        return parseEntityToResponse(entity.getHits().get(0), getSerializationScope());
    }

    protected Response editEntity(T entity, Class<?> serializationScope) {
        try {
            entityRepository.edit(entity);
            LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
        } catch (RollbackException ex) {
            LOGGER.debug("Provided value isn't unique");
            return responseGenerator.error(400, errorNotUnique);
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return responseGenerator.error(400, errorMissingFields);
        } catch (InternalLogicException ex) {
            //TODO
        }

        Optional<T> after = entityRepository.find(entity.getPrimaryKey(),getEntityClass());

        return parseEntityToResponse(after.get(), serializationScope);
    }

    /**
     * Parsed the entity to a JSON response
     *
     * @param entity the entity to parse
     * @param serializationScope the jackson serialization scope
     *
     * @return the response to be delivered to the client
     */
    protected Response parseEntityToResponse(T entity, Class<?> serializationScope) {
        try {
            String result = JsonUtil.toJson(entity, serializationScope);
            return responseGenerator.success(JsonUtil.fromJson(result));
        } catch (InternalJsonException ex) {
            LOGGER.error("Unable to parse database entity to JSON {}", ex.getMessage());
            return responseGenerator.error(500, errorCannotParse);
        }
    }

    protected T parseJSONIntoExistingEntity(String x, Class<?> serializationScope) {
        Optional<T> entity = entityRepository.find(JsonUtil.fromJson(x, getEntityClass()).getPrimaryKey(), getEntityClass());
        if (entity.isEmpty()) {
            throw new InternalLogicException("");
        }
        return JsonUtil.updateExistingEntity(entity.get(), x,getEntityClass(), serializationScope);
    }

    protected Class<?> getSerializationScope() {
        Class<?> serializationScope = Views.Public.class;

        if (requestDetails.getUser().isPresent()) {
            if (requestDetails.isInRole(ENTITY_PERM + getEntityClass().getSimpleName())) {
                serializationScope = Views.Owner.class;
            }

            if (requestDetails.isInRole(PERM_INTERNAL)) {
                serializationScope = Views.Internal.class;
            }
        }
        return serializationScope;
    }

    public void setEntityRepository(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }
}
