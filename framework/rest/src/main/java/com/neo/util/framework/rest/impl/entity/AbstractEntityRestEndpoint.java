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
import com.neo.util.framework.rest.api.RestAction;
import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.rest.impl.DefaultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

public abstract class AbstractEntityRestEndpoint<T extends DataBaseEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityRestEndpoint.class);

    protected static final ObjectNode E_NOT_FOUND = DefaultResponse.errorObject("pgs/resources/000","Entity not found");
    protected static final ObjectNode E_CANNOT_PARSE = DefaultResponse.errorObject("pgs/resources/001","Unable to retrieve entity");
    protected static final ObjectNode E_MISSING_FIELDS = DefaultResponse.errorObject("pgs/resources/002","Entity is missing mandatory fields");
    protected static final ObjectNode E_NOT_UNIQUE = DefaultResponse.errorObject("pgs/resources/003","Provided value isn't unique");
    protected static final ObjectNode E_INVALID_RESOURCE_PERMISSION = DefaultResponse.errorObject("pgs/resources/004","Invalid resource permission");

    protected static final String ENTITY_PERM = "CRUD_";

    protected static final String PERM_INTERNAL = "internal";

    @Inject
    protected EntityRepository entityRepository;

    @Inject
    protected RequestDetails requestDetails;

    protected abstract Object convertToPrimaryKey(String primaryKey);

    protected abstract Class<T> getEntityClass();

    protected RestAction getByPrimaryKeyAction(String primaryKey) {
        return () -> entityByColumn(DataBaseEntity.C_ID, convertToPrimaryKey(primaryKey));
    }

    protected RestAction getByValueAction(String column, String value) {
        return () -> entityByColumn(column, value);
    }

    protected RestAction createAction(String x) {
        return  () -> {
            T entity = JsonUtil.fromJson(x, getEntityClass(), getSerializationScope());
            try {
                entityRepository.create(entity);
                LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
            } catch (RollbackException ex) {
                LOGGER.debug("Provided value isn't unique");
                return DefaultResponse.error(400, E_NOT_UNIQUE, requestDetails.getRequestContext());
            } catch (PersistenceException ex) {
                LOGGER.debug("Entity is missing mandatory fields");
                return DefaultResponse.error(400, E_MISSING_FIELDS, requestDetails.getRequestContext());
            }
            return parseEntityToResponse(entity,Views.Owner.class);
        };
    }

    public RestAction editAction(String x) {
        return () -> editEntity(parseJSONIntoExistingEntity(x, getSerializationScope()), getSerializationScope());
    }

    public RestAction deleteAction(String primaryKey) {
        return () -> {
            Optional<T> entity = entityRepository.find(convertToPrimaryKey(primaryKey), getEntityClass());

            if (entity.isEmpty()) {
                LOGGER.debug("Entity not found [{},{}]", getEntityClass().getSimpleName() ,primaryKey);
                return DefaultResponse.error(404, E_NOT_FOUND, requestDetails.getRequestContext());
            }

            try {
                entityRepository.remove(entity.get());
                LOGGER.info("Deleted entity [{},{}]",getEntityClass().getSimpleName(), entity.get().getPrimaryKey());
                return DefaultResponse.success(requestDetails.getRequestContext());
            } catch (RollbackException ex) {
                return DefaultResponse.error(400, E_MISSING_FIELDS, requestDetails.getRequestContext());
            }
        };
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
            return DefaultResponse.error(404, E_NOT_FOUND, requestDetails.getRequestContext());
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
            return DefaultResponse.error(400, E_NOT_UNIQUE, requestDetails.getRequestContext());
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return DefaultResponse.error(400, E_MISSING_FIELDS, requestDetails.getRequestContext());
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
            return DefaultResponse.success(requestDetails.getRequestContext(), JsonUtil.fromJson(result));
        } catch (InternalJsonException ex) {
            LOGGER.error("Unable to parse database entity to JSON {}", ex.getMessage());
            return DefaultResponse.error(500, E_CANNOT_PARSE, requestDetails.getRequestContext());
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

        if (requestDetails.getUUId().isPresent()) {
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
