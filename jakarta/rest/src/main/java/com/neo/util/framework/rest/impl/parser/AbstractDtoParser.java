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
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * This abstract class used as a base for classes generated by {@link InboundDtoProcessor}
 *
 * @param <T> the object the input stream should be parsed into
 */
@Consumes({"application/json", "text/json", "*/*"})
public abstract class AbstractDtoParser<T> implements MessageBodyReader<T> {

    protected static final ExceptionDetails EX_UNKNOWN_JSON_SCHEMA = new ExceptionDetails(
            "framework/json/unknown-schema", "Invalid json schema to check against {0}.", true
    );

    @Inject
    protected JsonSchemaLoader jsonSchemaLoader;

    protected JsonSchema schema;

    protected final Class<T> clazz;

    protected AbstractDtoParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected abstract String getSchemaLocation();


    @PostConstruct
    protected void init() {
        schema = Optional.ofNullable(jsonSchemaLoader.getUnmodifiableMap().get(getSchemaLocation()))
                .orElseThrow(() -> new ConfigurationException(EX_UNKNOWN_JSON_SCHEMA, getSchemaLocation()));
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return clazz.equals(aClass);
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws WebApplicationException {
        try {
            JsonNode input = JsonUtil.fromJson(entityStream);
            JsonSchemaUtil.isValidOrThrow(input, schema);
            return JsonUtil.fromJson(input, type);
        } catch (ValidationException ex) {
            throw ExceptionUtils.asExternal(ex);
        }
    }
}