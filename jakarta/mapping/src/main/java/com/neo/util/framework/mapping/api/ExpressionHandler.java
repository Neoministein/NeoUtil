package com.neo.util.framework.mapping.api;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.node.ExpressionNode;
import com.neo.util.framework.mapping.api.node.Node;
import jakarta.el.ValueExpression;

import java.util.Optional;

public interface ExpressionHandler {

    Node parseSingleValue(String fieldName, String expression, String defaultValue, JsonDataType dataType);

    ValueExpression createExpression(String expression, Class<?> resultType);

    Optional<Object> staticEvaluateExpression(ValueExpression expression);

    Object evaluateWithContext(ExpressionNode expressionNode);
}
