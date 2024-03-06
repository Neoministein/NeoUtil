package com.neo.util.framework.rest.impl.entity;

import com.neo.util.common.api.json.Views;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.persistence.criteria.ExplicitSearchCriteria;
import com.neo.util.framework.api.persistence.entity.EntityProvider;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.api.request.UserRequest;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * This abstract class provides the base CRUD functionality for {@link PersistenceEntity}
 *
 * @param <T> the {@link PersistenceEntity} to be accessed
 */
public abstract class AbstractEntityRestEndpoint<T extends PersistenceEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityRestEndpoint.class);

    public static final ExceptionDetails EX_ENTITY_NOT_FOUND = new ExceptionDetails(
            "resource/not-found", "Cannot find resource {0}");
    public static final ExceptionDetails EX_ENTITY_NONE_UNIQUE = new ExceptionDetails(
            "resource/none-unique", "");
    public static final ExceptionDetails EX_ENTITY_MISSING_FIELDS = new ExceptionDetails(
            "resource/already-exists", "");
    public static final String ENTITY_PERM = "CRUD_";

    public static final String PERM_INTERNAL = "internal";

    @Inject
    protected EntityProvider entityRepository;

    @Inject
    @UserRequest
    protected UserRequestDetails userRequestDetails;

    @Inject
    protected ClientResponseService clientResponseService;

    protected abstract Object convertToPrimaryKey(String primaryKey);

    protected abstract Class<T> getEntityClass();

    protected Response getByPrimaryKey(String primaryKey) {
        return entityByColumn(PersistenceEntity.C_ID, convertToPrimaryKey(primaryKey));
    }

    protected Response getByValue(String column, String value) {
        return entityByColumn(column, value);
    }

    protected Response create(String x) {
        T entity;
        try {
            entity = JsonUtil.fromJson(x, getEntityClass(), getSerializationScope());
        } catch (ValidationException ex) {
            throw ex.asExternal();
        }

        try {
            entityRepository.create(entity);
            LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return clientResponseService.error(400, EX_ENTITY_MISSING_FIELDS);
        } catch (Exception ex) {
            LOGGER.debug("Provided value isn't unique");
            return clientResponseService.error(400, EX_ENTITY_NONE_UNIQUE);
        }
        return parseToResponse(entity,Views.Owner.class);
    }

    protected Response edit(String x) {
        return editEntity(parseJSONIntoExistingEntity(x, getSerializationScope()), getSerializationScope());
    }

    protected Response delete(String primaryKey) {
        Optional<T> entity = entityRepository.fetch(convertToPrimaryKey(primaryKey), getEntityClass());

        if (entity.isEmpty()) {
            LOGGER.debug("Entity not found [{},{}]", getEntityClass().getSimpleName() ,primaryKey);
            return clientResponseService.error(404, EX_ENTITY_NOT_FOUND, primaryKey);
        }

        try {
            entityRepository.remove(entity.get());
            LOGGER.info("Deleted entity [{},{}]",getEntityClass().getSimpleName(), entity.get().getPrimaryKey());
            return Response.ok().build();
        } catch (PersistenceException ex) {
            return clientResponseService.error(400, EX_ENTITY_MISSING_FIELDS);
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
        EntityResult<T> entity = entityRepository.fetch(entityParameters);
        if (entity.getHitSize() == 0) {
            LOGGER.debug("Entity not found [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
            return clientResponseService.error(404, EX_ENTITY_NOT_FOUND, value);
        }
        LOGGER.trace("Entity lookup success [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
        return parseToResponse(entity.getHits().get(0), getSerializationScope());
    }

    protected Response editEntity(T entity, Class<?> serializationScope) {
        try {
            entityRepository.edit(entity);
            LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return clientResponseService.error(400, EX_ENTITY_MISSING_FIELDS);
        } catch (Exception ex) {
            LOGGER.debug("Provided value isn't unique");
            return clientResponseService.error(400, EX_ENTITY_NONE_UNIQUE);
        }
        return parseToResponse(entity, serializationScope);
    }

    /**
     * Parsed the entity to a JSON response
     *
     * @param object the object to parse
     * @param serializationScope the jackson serialization scope
     *
     * @return the response to be delivered to the client
     */
    protected Response parseToResponse(Object object, Class<?> serializationScope) {
        return Response.ok().entity(JsonUtil.fromPojo(object, serializationScope).toString()).build();
    }

    protected T parseJSONIntoExistingEntity(String x, Class<?> serializationScope) {
        Object primaryKey =  JsonUtil.fromJson(x, getEntityClass(), serializationScope).getPrimaryKey();

        Optional<T> entity = entityRepository.fetch(primaryKey, getEntityClass());
        if (entity.isEmpty()) {
            throw new NoContentFoundException(EX_ENTITY_NOT_FOUND, primaryKey);
        }
        return JsonUtil.updateExistingEntity(entity.get(), x, getEntityClass(), serializationScope);
    }

    protected Class<?> getSerializationScope() {
        Class<?> serializationScope = Views.Public.class;

        if (userRequestDetails.getUser().isPresent()) {
            if (userRequestDetails.isInRole(ENTITY_PERM + getEntityClass().getSimpleName())) {
                serializationScope = Views.Owner.class;
            }

            if (userRequestDetails.isInRole(PERM_INTERNAL)) {
                serializationScope = Views.Internal.class;
            }
        }
        return serializationScope;
    }
}
