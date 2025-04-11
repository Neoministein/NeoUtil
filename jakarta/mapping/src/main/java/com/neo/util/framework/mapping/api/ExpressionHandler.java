package com.neo.util.framework.mapping.api;

import com.neo.util.common.api.json.JsonDataType;

import java.util.Optional;

public interface ExpressionHandler {

    ExpressionValue createExpression(String name, String expression, JsonDataType dataType);

    Optional<Object> staticEvaluateExpression(ExpressionValue expression);

    Object evaluateWithContext(ExpressionValue expressionValue);
}
