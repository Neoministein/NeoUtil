package com.neo.util.framework.impl.json;

import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.networknt.schema.JsonSchema;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * This class loads all {@link JsonSchema} on startup and provided an UnmodifiableMap {@link Map} for all classes which require them.
 * <br>
 * This increases startup time however decreases the response time of the first request which requires a particular schema.
 */
@ApplicationScoped
public class JsonSchemaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaLoader.class);

    protected Map<String, JsonSchema> jsonSchemaMap;

    @Inject
    protected ConfigService configService;

    @PostConstruct
    public void init() {
        LOGGER.info("Pre-loading json schemas");
        Map<String, JsonSchema> mapToFill = new HashMap<>();

        String relativePath = FrameworkConstants.JSON_SCHEMA_LOCATION.concat("/");

        try {
            for (String filePath: ResourceUtil.getResourceFileAsList(FrameworkConstants.JSON_SCHEMA_INDEX)) {
                addSchema(mapToFill, relativePath,
                        filePath.substring(relativePath.length()));
            }
        } catch (ConfigurationException ex) {
            LOGGER.warn("Unable to load json schema index [{}]. Falling back to reflections", ex.getMessage());
            for (String filePath: ReflectionUtils.getResources(
                    FrameworkConstants.JSON_SCHEMA_LOCATION, ReflectionUtils.JSON_FILE_ENDING)) {
                addSchema(mapToFill, relativePath,
                        filePath.substring(relativePath.length()));
            }
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

    public Optional<JsonSchema> getJsonSchema(String path) {
        JsonSchema schema = jsonSchemaMap.get(path);
        if (schema != null) {
            return Optional.of(schema);
        }
        LOGGER.warn("The json schema [{}] does not exist", path);
        return Optional.empty();
    }
}
