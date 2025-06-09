package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.api.node.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

@ApplicationScoped
public class MappingServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingServiceImpl.class);

    protected final ExpressionHandler expressionHandler;
    protected final Provider<RequestContextController> requestContextControllerProvider;
    protected final Provider<MappingStateHolder> mappingStateHolderProvider;

    @Inject
    public MappingServiceImpl(ExpressionHandler expressionHandler, Provider<RequestContextController> requestContextControllerProvider, Provider<MappingStateHolder> mappingStateHolderProvider) {
        this.expressionHandler = expressionHandler;
        this.requestContextControllerProvider = requestContextControllerProvider;
        this.mappingStateHolderProvider = mappingStateHolderProvider;
    }


    public JsonNode transformJson(MappingSchema mapping, ObjectNode sourceJson) {
        LOGGER.info("Transforming Json for mapping [{}]", mapping.getName());
        LOGGER.debug("Input Json: {}", sourceJson);
        Optional<String> error = JsonSchemaUtil.isValid(sourceJson, mapping.getInputSchema());
        if (error.isPresent()) {
            LOGGER.error("The source json doesn't add hear to the required schema error: [{}]", error.get());
            throw new IllegalArgumentException("The source json doesn't add hear to the required schema error: [" + error.get() +"]");
        }

        RequestContextController rcc = requestContextControllerProvider.get();
        rcc.activate();
        try {
            mappingStateHolderProvider.get().init(sourceJson);
            return parseObject(mapping.getMappingSchema());
        } finally {
            mappingStateHolderProvider.get().clear();
            rcc.deactivate();
        }
    }

    private void parseElement(Node schemaNode, ObjectNode destinationNode) {
        LOGGER.debug("Parsing [{}], schemaType [{}]", schemaNode.getFiledName(), schemaNode.getDataType());
        if (schemaNode.getSkipExpression().isPresent()
                && (boolean) expressionHandler.evaluateWithContext(schemaNode.getSkipExpression().get())) {
            LOGGER.debug("Skipping [{}], skip expression evaluated to true", schemaNode.getFiledName());
            return;
        }

         JsonNode resultNode = switch (schemaNode) {
            case StaticNode staticNode -> JsonUtil.fromPojo(staticNode.getSchemaValue());
            case ExpressionNode expressionNode -> parseSingleValue(expressionNode);
            case NestedObjectNode nestedObjectNode -> parseObject(nestedObjectNode);
            case LoopArrayNode loopArrayNode -> parseArray(loopArrayNode);
            case SingleArrayNode singleArrayNode -> parseArray(singleArrayNode);

            default -> throw new IllegalArgumentException("The provided schemaNode Class isn't implemented. Class: [" + schemaNode.getClass().getName() + "]");
        };

        if (schemaNode.getDataType().equals(JsonDataType.ARRAY)) {
            ArrayNode arrayNode = destinationNode.withArray(schemaNode.getFiledName());
            arrayNode.addAll((ArrayNode) resultNode);
        } else {
            destinationNode.set(schemaNode.getFiledName(), resultNode);
        }
    }

    private JsonNode parseSingleValue(ExpressionNode nestedObjectNode) {
        Object evaluatedExpression = expressionHandler.evaluateWithContext(nestedObjectNode.getExpression());
        LOGGER.debug("SingleValueExpression [{}] evaluated to [{}]", nestedObjectNode.getFiledName(), evaluatedExpression);
        return JsonUtil.fromPojo(evaluatedExpression);
    }

    private JsonNode parseObject(NestedNode nestedObjectNode) {
        ObjectNode objectNode = JsonUtil.emptyObjectNode();

        if (nestedObjectNode.getDefaultValue().isPresent()) {
            Object result = expressionHandler.evaluateWithContext(nestedObjectNode.getDefaultValue().get());
            if (result instanceof ObjectNode node) {
                objectNode = node;
            }
        }

        for (Node node: nestedObjectNode.getChildren()) {
            parseElement(node, objectNode);
        }
        return objectNode;
    }

    private ArrayNode parseArray(LoopArrayNode loopArrayNode) {
        ArrayNode arrayNode = JsonUtil.emptyArrayNode();
        MappingStateHolder mappingStateHolder = mappingStateHolderProvider.get();
        Object loopSource = expressionHandler.evaluateWithContext(loopArrayNode.getLoopExpression());
        String loopSourceClass = loopSource == null ? null : loopSource.getClass().getName();
        LOGGER.debug("LoopSourceExpression [{}] evaluated with class [{}]", loopArrayNode.getFiledName(), loopSourceClass);
        if (loopSource instanceof ArrayNode source) {
            for (int i = 0; i < source.size(); i++) {
                mappingStateHolder.setLoopIteration(i, loopArrayNode.getVarName(), source.get(i));
                arrayNode.add(parseObject(loopArrayNode));
            }
        } else if (loopSource instanceof Collection<?> source) {
            int i = 0;
            for (Object entry: source) {
                i++;
                mappingStateHolder.setLoopIteration(i, loopArrayNode.getVarName(), entry);
                arrayNode.add(parseObject(loopArrayNode));
            }
        } else if (loopSource instanceof JsonNode) {
            mappingStateHolder.setLoopIteration(0, loopArrayNode.getVarName(), loopArrayNode);
            arrayNode.add(parseObject(loopArrayNode));

        } else if (loopSource != null) {
            throw new IllegalArgumentException("Unsupported LoopSource Type [" + loopSource.getClass().getName() + "]");
        }
        mappingStateHolder.removeLoopIteration(loopArrayNode.getVarName());

        return arrayNode;
    }

    private ArrayNode parseArray(SingleArrayNode loopArrayNode) {
        ArrayNode arrayNode = JsonUtil.emptyArrayNode();

        arrayNode.add(parseObject(loopArrayNode));
        return arrayNode;
    }
}
