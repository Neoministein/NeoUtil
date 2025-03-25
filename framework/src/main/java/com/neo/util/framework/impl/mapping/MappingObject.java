package com.neo.util.framework.impl.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class MappingObject {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Document mapping;

    public MappingObject(String xmlMapping) {
        this.mapping = parseXml(xmlMapping);
    }

    public JsonNode transformJson(ObjectNode sourceJson) {
        Element root = mapping.getDocumentElement();

        ObjectNode destinationNode = objectMapper.createObjectNode();
        parseElement(root, sourceJson, destinationNode);
        return destinationNode;
    }

    private void parseElement(Element element, ObjectNode sourceJson, ObjectNode destinationNode) {
        String tag = element.getTagName();
        String type = element.getAttribute("type");
        String value = element.getAttribute("value");
        boolean required = Boolean.parseBoolean(element.getAttribute("required"));


        JsonNode node = null;
        if ("String".equals(type) || "Integer".equals(type) || "Float".equals(type) || "Boolean".equals(type)) {
            node = parseSingleValue(required, type, value, sourceJson);
        } else if ("object".equals(type)) {
            node =  parseObject(element, sourceJson);
        } else if ("array".equals(type)) {
            node =  parseArray(required, element, sourceJson);
        }

        if (type.equals("array")) {
            ArrayNode arrayNode = destinationNode.withArray(tag);
            arrayNode.addAll((ArrayNode) node);
        } else {
            destinationNode.set(tag, node);
        }
    }

    private JsonNode parseSingleValue(boolean required, String type, String value, ObjectNode sourceJson) {
        JsonNode node = parseDataType(type, evaluateExpress(value, sourceJson));

        if (required && node.isNull()) {
            throw new IllegalArgumentException();
        }

        return node;
    }

    private String evaluateExpress(String value, ObjectNode sourceJson) {
        StringBuilder sb = new StringBuilder();
        for (ExpressExtractor.Value a : ExpressExtractor.extractParts(value)) {
            if (a.type() == ExpressExtractor.Type.EXPRESSION) {
                String jsonPath = value.substring(2, value.length() - 1);
                sb.append(extractJsonValue(jsonPath, sourceJson).asText());
            } else if (a.type() == ExpressExtractor.Type.STATIC) {
                sb.append(a.value());
            }
        }
        return sb.toString();
    }

    private JsonNode parseDataType(String type, String value) {
        if (value == null) {
            return objectMapper.nullNode();
        }

        return objectMapper.valueToTree(
                switch (type) {
                    case "String" -> value;
                    case "Integer" -> Integer.parseInt(value);
                    case "Float" -> Float.parseFloat(value);
                    case "Boolean" -> Boolean.parseBoolean(value);
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

    private JsonNode parseObject(Element element, ObjectNode sourceJson) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                parseElement(childElement, sourceJson, objectNode);
            }
        }
        return objectNode;
    }

    private ArrayNode parseArray(boolean required, Element element, ObjectNode sourceJson) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        String loopPath = element.getAttribute("loop");
        String varName = element.getAttribute("var");

        if (!loopPath.isEmpty()) { // ✅ Case 1: Dynamic looping (existing behavior)
            JsonNode loopSource = extractJsonValue(loopPath.substring(2, loopPath.length() - 1), sourceJson);
            if (required && loopSource.isNull()) {
                throw new IllegalArgumentException();
            }

            if (loopSource.isArray()) {
                for (int i = 0; i < loopSource.size(); i++) {
                    ObjectNode item = (ObjectNode) loopSource.get(i);
                    item.put("_iteration", i);
                    sourceJson.set(varName, item);
                    arrayNode.add(parseObject(element, sourceJson));
                    item.remove("_iteration");
                    sourceJson.remove(varName);
                }
            }
        } else { // ✅ Case 2: Manually defined array in XML (single iteration)
            NodeList children = element.getChildNodes();
            ObjectNode itemNode = objectMapper.createObjectNode();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    Element childElement = (Element) children.item(i);
                    parseElement(childElement, sourceJson, itemNode);
                }
            }
            arrayNode.add(itemNode);
        }

        return arrayNode;
    }

    private Document parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.replace("\n", "").getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
