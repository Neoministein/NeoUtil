package com.neo.util.framework.impl.json;

import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.networknt.schema.JsonSchema;
import jakarta.annotation.PostConstruct;
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
 * This increases startup time however decreases the first response time of the first request which require a particular schema.
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

        addSchemas(mapToFill, ResourceUtil.getFolderContent(FrameworkConstants.JSON_SCHEMA_LOCATION),
                FrameworkConstants.JSON_SCHEMA_LOCATION, "");

        ConfigValue<String> jsonSchemaFolder = configService.get("json").get("schemaFolder").asString();
        if (jsonSchemaFolder.isPresent()) {
            addSchemas(mapToFill, ResourceUtil.getFolderContent(jsonSchemaFolder.get()),
                    jsonSchemaFolder.get(), "");
        }

        jsonSchemaMap = Collections.unmodifiableMap(mapToFill);
    }

    protected void addSchemas(Map<String, JsonSchema> mapToFill ,File[] files, String jsonSchemaFolder, String currentPath) {
        for (File file: files) {
            if (file.isDirectory()) {
                addSchemas(mapToFill, file.listFiles(),jsonSchemaFolder ,currentPath + file.getName() + "/");
            } else {
                LOGGER.debug("Loading schema : {}{}", currentPath, file.getName());
                mapToFill.put(currentPath + file.getName(), JsonSchemaUtil.generateSchemaFromResource(jsonSchemaFolder+ "/"+ currentPath + file.getName()));
            }
        }
    }


    public void onStartUp(@Observes ApplicationPreReadyEvent applicationPreReadyEvent) {
        LOGGER.debug("Startup event received");
    }

    public Map<String, JsonSchema> getUnmodifiableMap() {
        return jsonSchemaMap;
    }

    public Optional<JsonSchema> getJsonSchema(String path) {
        return Optional.ofNullable(jsonSchemaMap.get(path));
    }
}
