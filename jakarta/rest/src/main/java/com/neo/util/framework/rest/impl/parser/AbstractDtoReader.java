package com.neo.util.framework.rest.impl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ExceptionUtils;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.impl.json.JsonSchemaLoader;
import com.networknt.schema.JsonSchema;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This abstract class used as a base for classes {@link MessageBodyReader}.
 *
 * @param <T> the object the input stream should be parsed into
 */
@Consumes({"application/json", "text/json", "*/*"})
public abstract class AbstractDtoReader<T> implements MessageBodyReader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDtoReader.class);

    protected static final ExceptionDetails EX_UNKNOWN_JSON_SCHEMA = new ExceptionDetails(
            "framework/json/unknown-schema", "Invalid json schema to check against {0}.", true
    );

    @Inject
    protected JsonSchemaLoader jsonSchemaLoader;

    protected JsonSchema schema;

    protected final Class<T> clazz;

    protected AbstractDtoReader(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected abstract String getSchemaLocation();


    @PostConstruct
    protected void init() {
        LOGGER.trace("Registering DTO reader for class [{}]", clazz.getSimpleName());
        schema = jsonSchemaLoader.getJsonSchema(getSchemaLocation())
                .orElseThrow(() -> new DeploymentException(new ConfigurationException(EX_UNKNOWN_JSON_SCHEMA, getSchemaLocation())));
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return clazz.equals(aClass);
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws WebApplicationException {
        JsonNode input;
        try {
            input = JsonUtil.fromJson(entityStream);
        } catch (ValidationException ex) {
            throw ExceptionUtils.asExternal(ex);
        }

        try {
            JsonSchemaUtil.isValidOrThrow(input, schema);
            return JsonUtil.fromJson(input, type);
        } catch (ValidationException ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The parsed json does not match the dto schema [{}]", JsonUtil.toJson(input));
            }
            throw ExceptionUtils.asExternal(ex);
        }
    }
}
