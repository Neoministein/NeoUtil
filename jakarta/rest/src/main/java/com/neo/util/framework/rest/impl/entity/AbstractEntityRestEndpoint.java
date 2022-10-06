package com.neo.util.framework.rest.impl.entity;

import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.persistence.criteria.ExplicitSearchCriteria;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityRepository;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.framework.impl.connection.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.RollbackException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * This abstract class provides the base CRUD functionality for {@link PersistenceEntity}
 *
 * @param <T> the {@link PersistenceEntity} to be accessed
 */
public abstract class AbstractEntityRestEndpoint<T extends PersistenceEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityRestEndpoint.class);

    public static final String ENTITY_PERM = "CRUD_";

    public static final String PERM_INTERNAL = "internal";

    @Inject
    protected EntityRepository entityRepository;

    @Inject
    protected RequestDetails requestDetails;

    @Inject
    protected ResponseGenerator responseGenerator;

    @Inject
    protected EntityRestResponse entityRestResponse;

    protected abstract Object convertToPrimaryKey(String primaryKey);

    protected abstract Class<T> getEntityClass();

    protected Response getByPrimaryKey(String primaryKey) {
        return entityByColumn(PersistenceEntity.C_ID, convertToPrimaryKey(primaryKey));
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
            return responseGenerator.error(400, entityRestResponse.getNotUniqueError());
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return responseGenerator.error(400, entityRestResponse.getMissingFieldsError());
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
            return responseGenerator.error(404, entityRestResponse.getNotFoundError());
        }

        try {
            entityRepository.remove(entity.get());
            LOGGER.info("Deleted entity [{},{}]",getEntityClass().getSimpleName(), entity.get().getPrimaryKey());
            return responseGenerator.success();
        } catch (RollbackException ex) {
            return responseGenerator.error(400, entityRestResponse.getMissingFieldsError());
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
        if (entity.getHitSize() == 0) {
            LOGGER.debug("Entity not found [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
            return responseGenerator.error(404, entityRestResponse.getNotFoundError());
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
            return responseGenerator.error(400, entityRestResponse.getNotUniqueError());
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return responseGenerator.error(400, entityRestResponse.getMissingFieldsError());
        }
        return parseEntityToResponse(entity, serializationScope);
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
            return responseGenerator.error(500, entityRestResponse.getCannotParseError());
        }
    }

    protected T parseJSONIntoExistingEntity(String x, Class<?> serializationScope) {
        Optional<T> entity = entityRepository.find(JsonUtil.fromJson(x, getEntityClass()).getPrimaryKey(), getEntityClass());
        if (entity.isEmpty()) {
            throw new ClientErrorException(404);
        }
        return JsonUtil.updateExistingEntity(entity.get(), x, getEntityClass(), serializationScope);
    }

    protected Class<?> getSerializationScope() {
        Class<?> serializationScope = Views.Public.class;

        if (getRequestDetails().getUser().isPresent()) {
            if (getRequestDetails().isInRole(ENTITY_PERM + getEntityClass().getSimpleName())) {
                serializationScope = Views.Owner.class;
            }

            if (getRequestDetails().isInRole(PERM_INTERNAL)) {
                serializationScope = Views.Internal.class;
            }
        }
        return serializationScope;
    }

    protected HttpRequestDetails getRequestDetails() {
        return (HttpRequestDetails) requestDetails;
    }

    public void setEntityRepository(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }
}
