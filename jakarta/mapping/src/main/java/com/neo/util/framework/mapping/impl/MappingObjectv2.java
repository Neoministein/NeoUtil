package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.api.node.*;

public class MappingObjectv2 {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MappingSchema mapping;

    public MappingObjectv2(MappingSchema mappingSchema) {
        this.mapping = mappingSchema;
    }

    public JsonNode transformJson(ObjectNode sourceJson) {
        return parseObject(mapping.getSchemaNode(), sourceJson);
    }

    private void parseElement(Node schemaNode, ObjectNode sourceJson, ObjectNode destinationNode) {
         JsonNode resultNode = switch (schemaNode) {
            case StaticNode staticNode -> objectMapper.valueToTree(staticNode.getSchemaValue());
            case ExpressionNode expressionNode -> parseSingleValue(expressionNode, sourceJson);
            case NestedObjectNode nestedObjectNode -> parseObject(nestedObjectNode, sourceJson);
            case LoopArrayNode loopArrayNode -> parseArray(loopArrayNode, sourceJson);
            case SingleArrayNode singleArrayNode -> parseArray(singleArrayNode, sourceJson);

            default -> throw new IllegalStateException("Unexpected value: " + schemaNode);
        };

        if (schemaNode.getDataType().equals(JsonDataType.ARRAY)) {
            ArrayNode arrayNode = destinationNode.withArray(schemaNode.getFiledName());
            arrayNode.addAll((ArrayNode) resultNode);
        } else {
            destinationNode.set(schemaNode.getFiledName(), resultNode);
        }
    }

    private JsonNode parseSingleValue(ExpressionNode nestedObjectNode, ObjectNode sourceJson) {
        /*
        if (nestedObjectNode.getValues().size() == 1) {
            JsonNode node = extractJsonValue(nestedObjectNode.getValues().getFirst().value(), sourceJson);
            if (node.isEmpty()) {
                return objectMapper.valueToTree(nestedObjectNode.getDefaultValue());
            }
            return node;

        }
*/
        return parseDataType(nestedObjectNode.getDataType(), evaluateExpress(nestedObjectNode, sourceJson));
    }

    private String evaluateExpress(ExpressionNode nestedObjectNode, ObjectNode sourceJson) {
        /*
        StringBuilder sb = new StringBuilder();
        for (ExpressExtractor.Value a : nestedObjectNode.getValues()) {
            if (a.type() == ExpressExtractor.Type.EXPRESSION) {
                String jsonPath = a.value();
                sb.append(extractJsonValue(jsonPath, sourceJson).asText());
            } else if (a.type() == ExpressExtractor.Type.STATIC) {
                sb.append(a.value());
            }
        }*/
        return "";
    }

    private JsonNode parseDataType(JsonDataType  dataType, String value) {
        if (value == null) {
            return objectMapper.nullNode();
        }

        return objectMapper.valueToTree(
                switch (dataType) {
                    case STRING -> value;
                    case INTEGER -> Integer.parseInt(value);
                    case NUMBER -> Float.parseFloat(value);
                    case BOOLEAN -> Boolean.parseBoolean(value);
                    default -> null;
                });
    }

    private JsonNode extractJsonValue(String jsonPath, ObjectNode sourceJson) {
        String[] parts = jsonPath.split("\\.");
        JsonNode node = sourceJson;
        for (String part : parts) {
            node = node.path(part);
        }
        return node.isMissingNode() ? objectMapper.nullNode() : node;
    }

    private JsonNode parseObject(NestedNode nestedObjectNode, ObjectNode sourceJson) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (Node node: nestedObjectNode.getChildren()) {
            parseElement(node, sourceJson, objectNode);
        }
        return objectNode;
    }

    private ArrayNode parseArray(LoopArrayNode loopArrayNode, ObjectNode sourceJson) {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        JsonNode loopSource = extractJsonValue(loopArrayNode.getLoopPath().substring(2, loopArrayNode.getLoopPath().length() - 1), sourceJson);
        if (loopSource.isArray()) {
            for (int i = 0; i < loopSource.size(); i++) {
                ObjectNode item = (ObjectNode) loopSource.get(i);
                item.put("_iteration", i);
                sourceJson.set(loopArrayNode.getVarName(), item);
                arrayNode.add(parseObject(loopArrayNode, sourceJson));
                item.remove("_iteration");
                sourceJson.remove(loopArrayNode.getVarName());
            }
        }

        return arrayNode;
    }

    private ArrayNode parseArray(SingleArrayNode loopArrayNode, ObjectNode sourceJson) {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        ObjectNode itemNode = objectMapper.createObjectNode();
        for (Node node: loopArrayNode.getChildren()) {
            parseElement(node, sourceJson, itemNode);
        }
        arrayNode.add(itemNode);

        return arrayNode;
    }
}
