package com.neo.util.framework.mapping.impl;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.ExpressionValue;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.api.node.*;
import com.networknt.schema.JsonSchema;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MappingSchemaFactory {

    protected final ExpressionHandler expressionHandler;

    @Inject
    public MappingSchemaFactory(ExpressionHandler expressionHandler) {
        this.expressionHandler = expressionHandler;
    }

    public MappingSchema createSchema(String mappingXML, String inputJsonSchema) {
        return createSchema(parseXml(mappingXML), JsonSchemaUtil.generateNewSchema(inputJsonSchema));
    }

    public MappingSchema createSchema(Document mapping, JsonSchema inputSchema) {
        Element root = mapping.getDocumentElement();
        List<Node> nodes = getChildren(root);
        return new MappingSchema(new NestedObjectNode("root", nodes, null), inputSchema);
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
        return new NestedObjectNode(element.getTagName(), getChildren(element), getSkipable(element));
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
        return new ExpressionNode(dataType, tag, expressionHandler.createExpression(tag, value, dataType), getSkipable(element));
    }

    private Node parseArray(Element element) {
        String tag = element.getTagName();
        String loopExpression = element.getAttribute("loop");
        String varName = element.getAttribute("var");

        if (StringUtils.isEmpty(loopExpression) && StringUtils.isEmpty(varName)) {
            return new SingleArrayNode(tag, getChildren(element), getSkipable(element));
        }

        if (StringUtils.isPresent(loopExpression) && StringUtils.isPresent(loopExpression)) {
            return new LoopArrayNode(tag, varName, expressionHandler.createExpression(tag, loopExpression, JsonDataType.ARRAY), getChildren(element), getSkipable(element));
        }

        throw new IllegalArgumentException("The field [" + tag +"] either needs to have the attribute [loop] and [var] none of them");
    }

    private String getAndVerifyAttribute(Element element, String attribute) {
        if (element.hasAttribute(attribute)) {
            return element.getAttribute(attribute);
        }

        throw new IllegalArgumentException("The field [" + element.getTagName() + "] is missing the mandatory attribute ["+ attribute +"]");
    }

    private ExpressionValue getSkipable(Element element) {
        if (element.hasAttribute("skip")) {
            return expressionHandler.createExpression(element.getTagName() + ".skip" , element.getAttribute("skip"), JsonDataType.BOOLEAN);
        }

        return null;
    }

    private static Document parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.replace("\n", "").getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalArgumentException("The provided mapping isn't valid XML Error: ["+ ex.getMessage() +"]");
        }
    }

}
