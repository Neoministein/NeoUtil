package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.common.impl.json.JsonSchemaUtil;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.api.node.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Collection;
import java.util.Optional;

@ApplicationScoped
public class MappingServiceImpl {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected final ExpressionHandler expressionHandler;

    protected final Provider<RequestContextController> requestContextControllerProvider;

    protected final  Provider<MappingStateHolder> mappingStateHolderProvider;

    @Inject
    public MappingServiceImpl(ExpressionHandler expressionHandler, Provider<RequestContextController> requestContextControllerProvider, Provider<MappingStateHolder> mappingStateHolderProvider) {
        this.expressionHandler = expressionHandler;
        this.requestContextControllerProvider = requestContextControllerProvider;
        this.mappingStateHolderProvider = mappingStateHolderProvider;
    }


    public JsonNode transformJson(MappingSchema mapping, ObjectNode sourceJson) {
        Optional<String> i = JsonSchemaUtil.isValid(sourceJson, mapping.getInputSchema());
        if (i.isPresent()) {
            throw new IllegalArgumentException("The source json doesn't addhear to the requred schema [" + i.get() +"]");
        }

        RequestContextController rcc = requestContextControllerProvider.get();
        rcc.activate();
        try {
            mappingStateHolderProvider.get().init(sourceJson);
            return parseObject(mapping.getMappingSchema());
        } finally {
            rcc.deactivate();
        }
    }

    private void parseElement(Node schemaNode, ObjectNode destinationNode) {
        if (schemaNode.getSkipExpression().isPresent()
                && (boolean) expressionHandler.evaluateWithContext(schemaNode.getSkipExpression().get())) {
            return;
        }

         JsonNode resultNode = switch (schemaNode) {
            case StaticNode staticNode -> objectMapper.valueToTree(staticNode.getSchemaValue());
            case ExpressionNode expressionNode -> parseSingleValue(expressionNode);
            case NestedObjectNode nestedObjectNode -> parseObject(nestedObjectNode);
            case LoopArrayNode loopArrayNode -> parseArray(loopArrayNode);
            case SingleArrayNode singleArrayNode -> parseArray(singleArrayNode);

            default -> throw new IllegalArgumentException("The provided schemaNode isn't handledis n value: " + schemaNode);
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
        return objectMapper.valueToTree(evaluatedExpression);
    }

    private JsonNode parseObject(NestedNode nestedObjectNode) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (Node node: nestedObjectNode.getChildren()) {
            parseElement(node, objectNode);
        }
        return objectNode;
    }

    private ArrayNode parseArray(LoopArrayNode loopArrayNode) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        MappingStateHolder mappingStateHolder = mappingStateHolderProvider.get();
        Object loopSource = expressionHandler.evaluateWithContext(loopArrayNode.getLoopExpression());
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
        }
        mappingStateHolder.removeLoopIteration(loopArrayNode.getVarName());

        return arrayNode;
    }

    private ArrayNode parseArray(SingleArrayNode loopArrayNode) {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        ObjectNode itemNode = objectMapper.createObjectNode();
        for (Node node: loopArrayNode.getChildren()) {
            parseElement(node, itemNode);
        }
        arrayNode.add(itemNode);

        return arrayNode;
    }
}
