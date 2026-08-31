package com.neo.util.common.impl.json;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.common.impl.ResourceUtil;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.networknt.schema.*;
import com.networknt.schema.Error;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
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
     *  Make sure when calling this method, that the provided {@link Schema} has the fail fast {@link SchemaRegistryConfig}
     *  is set to true otherwise the validation doesn't throw an exception if one is found.
     *
     * @param jsonNode the node to check for validity
     * @param jsonSchema the schema to check against
     *
     * @throws ValidationException is thrown if it isn't valid
     */
    public static void isValidOrThrow(JsonNode jsonNode, Schema jsonSchema) {
        List<Error> errors = jsonSchema.validate(jsonNode);
        if (!errors.isEmpty()) {
            throw new ValidationException(EX_INVALID_JSON, errors.getFirst().getMessage());
        }
    }

    /**
     *  Make sure when calling this method, that the provided {@link Schema} has the fail fast {@link SchemaRegistryConfig}
     *  is set to true otherwise the validation doesn't throw an exception if one is found.
     *
     * @param jsonNode the node to check for validity
     * @param jsonSchema the schema to check against
     *
     * @return an optional that contains the error message if the validation failed
     */
    public static Optional<String> isValid(JsonNode jsonNode, Schema jsonSchema) {
        List<Error> errors = jsonSchema.validate(jsonNode);
        if (!errors.isEmpty()) {
            return Optional.of(errors.getFirst().getMessage());
        } else {
            return Optional.empty();
        }
    }

    public static ObjectNode generateSampleJson(Schema jsonSchema) {
        return (ObjectNode) generateSampleJson(jsonSchema.getSchemaNode());
    }

    public static JsonNode generateSampleJson(JsonNode schemaNode) {
        if (!schemaNode.has("type")) {
            return JsonUtil.emptyObjectNode();
        }

        return switch (JsonDataType.fromString(schemaNode.get("type").asString())) {
            case OBJECT -> {
                ObjectNode objectNode = JsonUtil.emptyObjectNode();
                if (schemaNode.has("properties")) {
                    JsonNode properties = schemaNode.get("properties");

                    for (Map.Entry<String, JsonNode> field: properties.properties()) {
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

        switch (JsonDataType.fromString(schemaNode.get("type").asString())) {
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
    public static Schema generateSchemaFromResource(String fileLocation) {
        return generateNewSchema(ResourceUtil.getResourceFileAsString(fileLocation), SpecificationVersion.DRAFT_2019_09);
    }

    /**
     * Generates a {@link Schema} from a json string with default schema version and config
     *
     * @param schema the schema as a json string
     *
     * @return the schema as an {@link Schema}
     */
    public static Schema generateNewSchema(String schema) {
        return generateNewSchema(schema, SpecificationVersion.DRAFT_2019_09);
    }

    /**
     * Generates a {@link Schema} from a json string with default config
     *
     * @param schema the schema as a json string
     * @param specVersion the Global json schema version
     *
     * @return the schema as an {@link Schema}
     *
     */
    public static Schema generateNewSchema(String schema, SpecificationVersion specVersion) {
        return generateNewSchema(schema,specVersion, getDefaultConfig());
    }

    /**
     * Generates a {@link Schema} from a json string
     *
     * @param schema the schema as a json string
     * @param specVersion the Global json schema version
     * @param config the schema config
     *
     * @return the schema as an {@link Schema}
     */
    public static Schema generateNewSchema(String schema, SpecificationVersion specVersion, SchemaRegistryConfig config) {
        try {
            Schema schemaObject = SchemaRegistry.withDefaultDialect(specVersion, builder -> builder.schemaRegistryConfig(config)).getSchema(schema);
            schemaObject.initializeValidators();
            return schemaObject;
        } catch (SchemaException ex) {
            throw new ValidationException(EX_INVALID_JSON_SCHEMA);
        }
    }

    /**
     * Returns the default configuration for a {@link Schema}
     *
     * @return the default configuration
     */
    public static SchemaRegistryConfig getDefaultConfig() {
        return SchemaRegistryConfig.builder().failFast(true).build();
    }
}
