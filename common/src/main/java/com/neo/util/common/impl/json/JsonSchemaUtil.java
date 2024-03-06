package com.neo.util.common.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.networknt.schema.*;

import java.util.Optional;

/**
 * This is a utility class for generating and checking valid json schemas.
 * <p>
 * The json Schema specification can be found here
 * <a href="https://json-schema.org/specification.html">https://json-schema.org/specification.html</a>
 */
public class JsonSchemaUtil {


    private static final ExceptionDetails EX_INVALID_JSON_SCHEMA = new ExceptionDetails(
            "common/json/invalid-schema", "The provided json schema is invalid");

    private static final ExceptionDetails EX_INVALID_JSON = new ExceptionDetails(
            "common/json/invalid-json", "{0}");

    private JsonSchemaUtil(){}

    /**
     *  Make sure when calling this method, that the provided {@link JsonSchema} has the fail fast {@link SchemaValidatorsConfig}
     *  is set to true otherwise the validation doesn't throw an exception if one is found.
     *
     * @param jsonNode the node to check for validity
     * @param jsonSchema the schema to check against
     *
     * @throws ValidationException is thrown if it isn't valid
     */
    public static void isValidOrThrow(JsonNode jsonNode, JsonSchema jsonSchema) {
        try {
            jsonSchema.validate(jsonNode);
        } catch (JsonSchemaException ex) {
            throw new ValidationException(EX_INVALID_JSON, ex.getMessage());
        }
    }

    /**
     *  Make sure when calling this method, that the provided {@link JsonSchema} has the fail fast {@link SchemaValidatorsConfig}
     *  is set to true otherwise the validation doesn't throw an exception if one is found.
     *
     * @param jsonNode the node to check for validity
     * @param jsonSchema the schema to check against
     *
     * @return an optional that contains the error message if the validation failed
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
        return generateNewSchema(ResourceUtil.getResourceFileAsString(fileLocation), SpecVersion.VersionFlag.V201909);
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
            throw new ValidationException(EX_INVALID_JSON_SCHEMA);
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
