package com.neo.util.framework.rest.impl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.InternalConfigurationException;
import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.ExternalJsonException;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.impl.json.JsonSchemaLoader;
import com.neo.util.framework.rest.api.parser.ValidateJsonSchema;
import com.networknt.schema.JsonSchema;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Provider
@ApplicationScoped
@Priority(Priorities.ENTITY_CODER)
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
public class JsonNodeParser implements MessageBodyReader<JsonNode> {

    protected final Map<String, JsonSchema> schemaMap;

    @Inject
    public JsonNodeParser(JsonSchemaLoader jsonSchemaLoader) {
        schemaMap = jsonSchemaLoader.getUnmodifiableMap();
    }

    @Context
    protected ResourceInfo resourceInfo;

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public JsonNode readFrom(Class<JsonNode> aClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> multivaluedMap, InputStream inputStream)
            throws IOException, WebApplicationException {
        try {
            JsonNode input = JsonUtil.fromJson(StringUtils.toString(inputStream, StandardCharsets.UTF_8));
            checkForSchema(input);
            return input;
        } catch (InternalJsonException ex) {
            throw new ExternalJsonException(ex);
        }
    }

    protected void checkForSchema(JsonNode input) {
        ValidateJsonSchema annotation = resourceInfo.getResourceMethod().getAnnotation(ValidateJsonSchema.class);
        if (annotation != null) {
            JsonSchemaUtil.isValidOrThrow(input, retrieveSchemaFromString(annotation.value()));
        }
    }

    protected JsonSchema retrieveSchemaFromString(String schemaLocation) {
        return Optional.ofNullable(schemaMap.get(schemaLocation))
                .orElseThrow(() -> new InternalConfigurationException("Invalid json schema to check against [" + schemaLocation + "]"));
    }
}
