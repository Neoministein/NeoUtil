package com.neo.util.javax.impl.rest.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.common.api.json.Views;
import com.neo.common.impl.exception.InternalJsonException;
import com.neo.common.impl.exception.InternalLogicException;
import com.neo.common.impl.json.JsonUtil;
import com.neo.javax.api.persitence.EntityParameters;
import com.neo.javax.api.persitence.EntityResult;
import com.neo.javax.api.persitence.criteria.ExplicitSearchCriteria;
import com.neo.javax.api.persitence.entity.DataBaseEntity;
import com.neo.javax.api.persitence.repository.EntityRepository;
import com.neo.util.javax.api.rest.RestAction;
import com.neo.util.javax.impl.rest.AbstractRestEndpoint;
import com.neo.util.javax.impl.rest.DefaultResponse;
import com.neo.util.javax.impl.rest.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractEntityRestEndpoint<T extends DataBaseEntity> extends AbstractRestEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityRestEndpoint.class);

    protected static final ObjectNode E_NOT_FOUND = DefaultResponse.errorObject("pgs/resources/000","Entity not found");
    protected static final ObjectNode E_CANNOT_PARSE = DefaultResponse.errorObject("pgs/resources/001","Unable to retrieve entity");
    protected static final ObjectNode E_MISSING_FIELDS = DefaultResponse.errorObject("pgs/resources/002","Entity is missing mandatory fields");
    protected static final ObjectNode E_NOT_UNIQUE = DefaultResponse.errorObject("pgs/resources/003","Provided value isn't unique");
    protected static final ObjectNode E_INVALID_RESOURCE_PERMISSION = DefaultResponse.errorObject("pgs/resources/004","Invalid resource permission");

    protected static final String ENTITY_PERM = "CRUD_";

    @Inject
    protected EntityRepository entityRepository;

    protected abstract Object convertToPrimaryKey(String primaryKey);

    protected abstract Class<T> getEntityClass();

    protected RestAction getByPrimaryKey(String primaryKey, RequestContext requestContext) {
        return () -> entityByColumn(DataBaseEntity.C_ID, convertToPrimaryKey(primaryKey), requestContext);
    }

    public RestAction create(String x, RequestContext requestContext) {
        return  () -> {
            T entity = JsonUtil.fromJson(x, getEntityClass(), getSerializationScope());
            try {
                entityRepository.create(entity);
                LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
            } catch (RollbackException ex) {
                LOGGER.debug("Provided value isn't unique");
                return DefaultResponse.error(400, E_NOT_UNIQUE, requestContext);
            } catch (PersistenceException ex) {
                LOGGER.debug("Entity is missing mandatory fields");
                return DefaultResponse.error(400, E_MISSING_FIELDS, requestContext);
            }
            return parseEntityToResponse(entity, requestContext, getSerializationScope());
        };
    }

    public RestAction edit(String x, RequestContext requestContext) {
        return () -> editEntity(parseJSONIntoExistingEntity(x, getSerializationScope()), getSerializationScope(), requestContext);
    }

    public RestAction delete(String primaryKey, RequestContext requestContext) {
        return () -> {
            Optional<T> entity = entityRepository.find(convertToPrimaryKey(primaryKey), getEntityClass());

            if (entity.isEmpty()) {
                LOGGER.debug("Entity not found [{},{}]", getEntityClass().getSimpleName() ,primaryKey);
                return DefaultResponse.error(404, E_NOT_FOUND, requestContext);
            }

            try {
                entityRepository.remove(entity.get());
                LOGGER.info("Deleted entity [{},{}]",getEntityClass().getSimpleName(), entity.get().getPrimaryKey());
                return DefaultResponse.success(requestContext);
            } catch (RollbackException ex) {
                return DefaultResponse.error(400, E_MISSING_FIELDS, requestContext);
            }
        };
    }

    /**
     * Retrieves an entity by a column value association and generates a response
     *
     * @param field the entity field
     * @param value the value which entity field must have
     * @param requestContext the resource location of the caller
     *
     * @return the response to be delivered to the client
     */
    protected Response entityByColumn(String field, Object value, RequestContext requestContext) {
        EntityParameters<T> entityParameters = new EntityParameters<>(getEntityClass(), 1, List.of(new ExplicitSearchCriteria(field, value)));
        EntityResult<T> entity = entityRepository.find(entityParameters);
        if (entity.getHitSize() == 1) {
            LOGGER.debug("Entity not found [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
            return DefaultResponse.error(404, E_NOT_FOUND, requestContext);
        }
        LOGGER.trace("Entity lookup success [{},{}:{}]", getEntityClass().getSimpleName(), field, value);
        return parseEntityToResponse(entity.getHits().get(0), requestContext, getSerializationScope());
    }

    protected Response editEntity(T entity, Class<?> serializationScope, RequestContext requestContext) {
        try {
            entityRepository.edit(entity);
            LOGGER.info("Created new [{},{}]",getEntityClass().getSimpleName(), entity.getPrimaryKey());
        } catch (RollbackException ex) {
            LOGGER.debug("Provided value isn't unique");
            return DefaultResponse.error(400, E_NOT_UNIQUE, requestContext);
        } catch (PersistenceException ex) {
            LOGGER.debug("Entity is missing mandatory fields");
            return DefaultResponse.error(400, E_MISSING_FIELDS, requestContext);
        } catch (InternalLogicException ex) {
            //TODO
        }

        Optional<T> after = entityRepository.find(entity.getPrimaryKey(),getEntityClass());

        return parseEntityToResponse(after.get(), requestContext, serializationScope);
    }

    /**
     * Parsed the entity to a JSON response
     *
     * @param entity the entity to parse
     * @param requestContext request context
     * @param serializationScope the jackson serialization scope
     *
     * @return the response to be delivered to the client
     */
    protected Response parseEntityToResponse(T entity, RequestContext requestContext, Class<?> serializationScope) {
        try {
            String result = JsonUtil.toJson(entity, serializationScope);
            return DefaultResponse.success(requestContext, JsonUtil.fromJson(result));
        } catch (InternalJsonException ex) {
            LOGGER.error("Unable to parse database entity to JSON {}", ex.getMessage());
            return DefaultResponse.error(500, E_CANNOT_PARSE, requestContext);
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

            if (requestDetails.isInRole("internal")) {
                serializationScope = Views.Internal.class;
            }
        }
        return serializationScope;
    }

    public void setEntityRepository(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }
}
