package com.neo.util.framework.mapping.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.impl.mapping.ExpressExtractor;
import com.neo.util.framework.mapping.api.node.LoopArrayNode;
import com.neo.util.framework.mapping.api.node.NestedObjectNode;
import com.neo.util.framework.mapping.api.node.Node;
import com.neo.util.framework.mapping.api.node.SingleArrayNode;
import com.networknt.schema.JsonSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MappingSchema {

    private final ExpressionHandler expressionHandler;
    private final JsonSchema inputSchema;
    private final JsonNode sampleJson;
    private final Map<String, JsonNode> loopValues = new HashMap<>();
    private final NestedObjectNode schemaNode;

    public MappingSchema(ExpressionHandler testName, String mappingXml, String inputSchema) {
        this(testName, parseXml(mappingXml), JsonSchemaUtil.generateNewSchema(inputSchema));
    }

    public MappingSchema(ExpressionHandler testName, Document mapping, JsonSchema inputSchema) {
        Element root = mapping.getDocumentElement();

        this.expressionHandler = testName;
        this.inputSchema = inputSchema;
        this.sampleJson = JsonSchemaUtil.generateSampleJson(inputSchema);
        this.schemaNode = new NestedObjectNode("root", getChildren(root));

    }

    private List<Node> getChildren(Element element) {
        List<Node> nodes = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element childElement) {
                nodes.add(parseElement(childElement));
            }
        }
        return nodes;
    }

    private NestedObjectNode parseObject(Element element) {
        return new NestedObjectNode(element.getTagName(), getChildren(element));
    }

    private Node parseElement(Element element) {
        JsonDataType dataType = JsonDataType.fromString(getAndVerifyAttribute(element, "type"));
        return switch (dataType) {
            case STRING, NUMBER, INTEGER, BOOLEAN -> parseSingleValue(element, dataType);
            case OBJECT -> parseObject(element);
            case ARRAY -> parseArray(element);
        };
    }

    private Node parseSingleValue(Element element, JsonDataType dataType) {
        String tag = element.getTagName();
        String value = getAndVerifyAttribute(element, "value");
        String defaultValue = element.hasAttribute("default") ? element.getAttribute("default") : null;
        return expressionHandler.parseSingleValue(tag, value, defaultValue, dataType);
    }

    public JsonNode getNode(String path) {
        String[] parts = path.split("\\.");

        if (parts.length == 0) {
            return JsonUtil.misingNode();
        }

        JsonNode node = loopValues.getOrDefault(parts[0], sampleJson.path(parts[0]));
        for (int i = 1; i < parts.length;i++) {
            node = node.path(parts[i]);
        }
        return node;
    }

    protected Optional<JsonDataType> validatePath(String path) {
        JsonNode node = getNode(path);

        if (node.isMissingNode()) {
            return Optional.empty();
        } else if (node.isTextual()) {
            return Optional.of(JsonDataType.STRING);
        } else if (node.isInt()) {
            return Optional.of(JsonDataType.INTEGER);
        } else if (node.isNumber()) {
            return Optional.of(JsonDataType.BOOLEAN);
        } else if (node.isArray()) {
            return Optional.of(JsonDataType.ARRAY);
        } else if (node.isObject()){
            return Optional.of(JsonDataType.OBJECT);
        } else {
            throw new RuntimeException(); //TODO
        }
    }

    private void validateExpression(List<ExpressExtractor.Value> valueList) {
        for (ExpressExtractor.Value value: valueList) {
            if (value.type() == ExpressExtractor.Type.EXPRESSION)  {
                if (validatePath(value.value()).isEmpty()) {
                    throw new RuntimeException(); //TODO
                }
            }
        }
    }

    private Node parseArray(Element element) {
        String tag = element.getTagName();
        String loopPath = element.getAttribute("loop");
        String varName = element.getAttribute("var");

        if (StringUtils.isEmpty(loopPath) && StringUtils.isEmpty(varName)) {
            return new SingleArrayNode(tag, getChildren(element));
        }

        if (StringUtils.isPresent(loopPath) && StringUtils.isPresent(loopPath)) {
            List<ExpressExtractor.Value> expressionValue = ExpressExtractor.extractParts(loopPath);
            if (expressionValue.isEmpty()) {
                throw new RuntimeException(); //TODO
            }
            if (expressionValue.getFirst().type() == ExpressExtractor.Type.STATIC) {
                throw new RuntimeException(); //TODO
            }

            Optional<JsonDataType> path = validatePath(expressionValue.getFirst().value());
            if (path.isEmpty() || !path.get().equals(JsonDataType.ARRAY)) {
                throw new RuntimeException(); //TODO
            }

            com.fasterxml.jackson.databind.node.ObjectNode node = (com.fasterxml.jackson.databind.node.ObjectNode) getNode(expressionValue.getFirst().value()).get(0);
            node.put("_iteration", 0);
            loopValues.put(varName, node);
            List<Node> children = getChildren(element);
            loopValues.remove(varName);

            return new LoopArrayNode(tag, varName, loopPath, children);
        }

        throw new RuntimeException(); //TODO
    }

    private String getAndVerifyAttribute(Element element, String attribute) {
        if (element.hasAttribute(attribute)) {
            return element.getAttribute(attribute);
        }

        throw new IllegalStateException(""); //TODO
    }

    private static Document parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.replace("\n", "").getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public NestedObjectNode getSchemaNode() {
        return schemaNode;
    }

    public ExpressionHandler getExpressionHandler() {
        return expressionHandler;
    }
}
