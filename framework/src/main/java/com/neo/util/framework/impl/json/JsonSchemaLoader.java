package com.neo.util.framework.impl.json;

import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.impl.ReflectionService;
import com.networknt.schema.JsonSchema;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class loads all {@link JsonSchema} on startup and provided an UnmodifiableMap {@link Map} for all classes which require them.
 * <br>
 * This increases startup time however decreases the response time of the first request which requires a particular schema.
 */
@ApplicationScoped
public class JsonSchemaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaLoader.class);

    public static final ExceptionDetails INVALID_SCHEDULER_ID = new ExceptionDetails(
            "json-schema/invalid-id", "The json-schema path [{0}] may not have whitespaces.");

    public static final String E_SCHEMA_DOES_NOT_EXIST = "json-schema/invalid-path";

    public static final ExceptionDetails EX_SCHEMA_DOES_NOT_EXIST = new ExceptionDetails(
            "json-schema/invalid-path", "The provided json schema path [{0}] does not exist.");

    protected final Map<String, JsonSchema> jsonSchemaMap;

    @Inject
    public JsonSchemaLoader(ConfigService configService, ReflectionService reflectionService) {
        LOGGER.info("Pre-loading json schemas");
        Map<String, JsonSchema> mapToFill = new HashMap<>();

        for (String filePath: reflectionService.getResources("^configuration/schema.*\\.json$")) {
            addSchema(mapToFill, FrameworkConstants.JSON_SCHEMA_LOCATION,
                    filePath.substring(FrameworkConstants.JSON_SCHEMA_LOCATION.length()));
        }

        configService.get("json.schema.externalFolder").asString().asOptional().ifPresent(config ->
                addSchemas(mapToFill, ResourceUtil.getFolderContent(config),
                        config.concat("/"), ""));

        jsonSchemaMap = Collections.unmodifiableMap(mapToFill);
    }

    protected void addSchemas(Map<String, JsonSchema> mapToFill ,File[] files, String jsonSchemaFolder, String currentPath) {
        for (File file: files) {
            if (file.isDirectory()) {
                addSchemas(mapToFill, file.listFiles(), jsonSchemaFolder ,currentPath + file.getName() + "/");
            } else {
                addSchema(mapToFill, jsonSchemaFolder, currentPath + file.getName());
            }
        }
    }

    protected void addSchema(Map<String, JsonSchema> mapToFill, String basePath, String relativePath) {
        LOGGER.debug("Loading schema at: [{}{}]", basePath, relativePath);
        if (relativePath.contains(" ")) {
            throw new ConfigurationException(INVALID_SCHEDULER_ID, relativePath);
        }
        JsonSchema schema = JsonSchemaUtil.generateSchemaFromResource(basePath.concat(relativePath));
        mapToFill.put(relativePath, schema);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Schema loaded: [{}]", schema.getSchemaNode().textValue());
        }
    }


    public void onStartUp(@Observes ApplicationPreReadyEvent applicationPreReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent processed");
    }

    public Map<String, JsonSchema> getUnmodifiableMap() {
        return jsonSchemaMap;
    }

    public Optional<JsonSchema> fetchJsonSchema(String path) {
        JsonSchema schema = jsonSchemaMap.get(path);
        if (schema != null) {
            return Optional.of(schema);
        }
        LOGGER.warn("The json schema [{}] does not exist", path);
        return Optional.empty();
    }

    public JsonSchema requestJsonSchema(String path) {
        return fetchJsonSchema(path).orElseThrow(() -> new NoContentFoundException(EX_SCHEMA_DOES_NOT_EXIST));
    }
}
