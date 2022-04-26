package com.neo.common.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.common.impl.RecourseUtil;
import com.neo.common.impl.exception.InternalJsonException;
import com.networknt.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * A Util class for generating and checking valid json schemas
 *
 * The json Schema specification can be found here https://json-schema.org/specification.html
 */
public class JsonSchemaUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaUtil.class);

    private JsonSchemaUtil(){}

    /**
     *  Make sure when calling this method, that the provided {@link JsonSchema} has the fail fast {@link SchemaValidatorsConfig}
     *  is set to true otherwise the validation doesn't throw an exception if one is found.
     *
     * @param jsonNode the node to check for validity
     * @param jsonSchema the schema to check against
     *
     * @throws InternalJsonException is thrown if is isn't valid
     */
    public static void isValidOrThrow(JsonNode jsonNode, JsonSchema jsonSchema) {
        try {
            jsonSchema.validate(jsonNode);
        } catch (JsonSchemaException ex) {
            throw new InternalJsonException(ex.getMessage());
        }
    }

    /**
     *  Make sure when calling this method, that the provided {@link JsonSchema} has the fail fast {@link SchemaValidatorsConfig}
     *  is set to true otherwise the validation doesn't throw an exception if one is found.
     *
     * @param jsonNode the node to check for validity
     * @param jsonSchema the schema to check against
     *
     * @return an optional that contains the error message if the validation vailed
     */
    public static Optional<String> isValid(JsonNode jsonNode, JsonSchema jsonSchema) {
        try {
            jsonSchema.validate(jsonNode);
            return Optional.empty();
        } catch (JsonSchemaException ex) {
            return Optional.of(ex.getMessage());
        }
    }

    /**
     * Generate the json form a file which resides in the projects resource folder.
     * Path example:
     * src/main/resources/[schemas/test.json] <- schemas/test.json
     *
     * @param fileLocation the fileLocation
     *
     * @return the generated JsonSchema
     */
    public static JsonSchema generateSchemaFromResource(String fileLocation) {
        try {
            return generateNewSchema(RecourseUtil.getResourceFileAsString(fileLocation), SpecVersion.VersionFlag.V201909);
        } catch (IOException ex) {
            LOGGER.error("Unable to retrieve json schema from file {}", ex.getMessage(), ex);
            throw new InternalJsonException("Unable to create a json schema file could not be read");
        }
    }

    /**
     * Generates a {@link JsonSchema} from a json string with default schema version and config
     *
     * @param schema the schema as a json string
     *
     * @return the schema as an {@link JsonSchema}
     */
    public static JsonSchema generateNewSchema(String schema) {
        return generateNewSchema(schema, SpecVersion.VersionFlag.V201909);
    }

    /**
     * Generates a {@link JsonSchema} from a json string with default config
     *
     * @param schema the schema as a json string
     * @param specVersion the Global json schema version
     *
     * @return the schema as an {@link JsonSchema}
     *
     */
    public static JsonSchema generateNewSchema(String schema, SpecVersion.VersionFlag specVersion) {
        return generateNewSchema(schema,specVersion, getDefaultConfig());
    }

    /**
     * Generates a {@link JsonSchema} from a json string
     *
     * @param schema the schema as a json string
     * @param specVersion the Global json schema version
     * @param config the schema config
     *
     * @return the schema as an {@link JsonSchema}
     */
    public static JsonSchema generateNewSchema(String schema, SpecVersion.VersionFlag specVersion, SchemaValidatorsConfig config) {
        try {
            return JsonSchemaFactory.getInstance(specVersion).getSchema(schema, config);
        } catch (JsonSchemaException ex) {
            throw new InternalJsonException("The provided json schema is invalid");
        }
    }

    /**
     * Returns the default configuration for a {@link JsonSchema}
     *
     * @return the default configuration
     */
    public static SchemaValidatorsConfig getDefaultConfig() {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setFailFast(true);
        return config;
    }
}
