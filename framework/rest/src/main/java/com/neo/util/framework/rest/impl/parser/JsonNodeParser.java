package com.neo.util.framework.rest.impl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.rest.api.parser.ValidateJsonSchema;
import com.networknt.schema.JsonSchema;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Provider
@ApplicationScoped
@Priority(Priorities.ENTITY_CODER)
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
public class JsonNodeParser implements MessageBodyReader<JsonNode> {

    protected final Map<String, JsonSchema> schemaMap = new HashMap<>();

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
        JsonNode input = JsonUtil.fromJson(StringUtils.toString(inputStream, StandardCharsets.UTF_8));
        checkForSchema(input);

        return input;
    }

    protected void checkForSchema(JsonNode input) {
        ValidateJsonSchema annotation = resourceInfo.getResourceMethod().getAnnotation(ValidateJsonSchema.class);
        if (annotation != null) {
            JsonSchemaUtil.isValidOrThrow(input, retrieveSchemaFromString(annotation.value()));
        }
    }

    protected JsonSchema retrieveSchemaFromString(String schemaLocation) {
        return schemaMap.computeIfAbsent(schemaLocation, JsonSchemaUtil::generateSchemaFromResource);
    }
}
