package com.neo.util.common.impl.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.networknt.schema.*;

import java.util.Iterator;
import java.util.Map;
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

    public static ObjectNode generateSampleJson(JsonSchema jsonSchema) {
        return (ObjectNode) generateSampleJson(jsonSchema.getSchemaNode());
    }

    public static JsonNode generateSampleJson(JsonNode schemaNode) {
        if (!schemaNode.has("type")) {
            return JsonUtil.emptyObjectNode();
        }

        return switch (JsonDataType.fromString(schemaNode.get("type").asText())) {
            case OBJECT -> {
                ObjectNode objectNode = JsonUtil.emptyObjectNode();
                if (schemaNode.has("properties")) {
                    JsonNode properties = schemaNode.get("properties");
                    Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        objectNode.set(field.getKey(), generateSampleJson(field.getValue()));
                    }
                }
                yield objectNode;
            }
            case ARRAY -> {
                if (schemaNode.has("items")) {
                    yield JsonUtil.emptyArrayNode().add(generateSampleJson(schemaNode.get("items")));
                }
                yield JsonUtil.emptyObjectNode();
            }
            case STRING -> JsonUtil.fromPojo("sample");
            case INTEGER -> JsonUtil.fromPojo(0);
            case NUMBER -> JsonUtil.fromPojo(0.0);
            case BOOLEAN -> JsonUtil.fromPojo(true);
        };
    }
/*
    public static Map<String, JsonDataType> extractAllFields(JsonSchema schema) {
        Map<String, JsonDataType> map = new HashMap<>();
        extractAllFields(map, schema.getSchemaNode(), "");
        return map;
    }

    public static void extractAllFields(Map<String, JsonDataType> map, JsonNode schemaNode, String node) {
        if (!schemaNode.has("type")) {
            return;
        }

        switch (JsonDataType.fromString(schemaNode.get("type").asText())) {
            case OBJECT:
                ObjectNode objectNode = JsonUtil.emptyObjectNode();
                if (schemaNode.has("properties")) {
                    JsonNode properties = schemaNode.get("properties");
                    Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        extractAllFields(map, field.getValue(), node + "." + field.getKey());
                    }
                }
                break;
            case ARRAY:
                if (schemaNode.has("items")) {
                    extractAllFields(map, schemaNode.get("items"), node);
                }
                break;
            case STRING:
                map.put(node, JsonDataType.STRING);
                break;
            case INTEGER:
                map.put(node, JsonDataType.INTEGER);
                break;
            case NUMBER:
                map.put(node, JsonDataType.NUMBER);
                break;
            case BOOLEAN:
                map.put(node, JsonDataType.BOOLEAN);
                break;
            default:
                break;
        }
    }
*/
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
