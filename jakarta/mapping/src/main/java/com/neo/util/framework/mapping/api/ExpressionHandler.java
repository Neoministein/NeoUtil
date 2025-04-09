package com.neo.util.framework.mapping.api;

import com.neo.util.common.api.json.JsonDataType;
import jakarta.el.ValueExpression;

import java.util.Optional;

public interface ExpressionHandler {

    ExpressionValue createExpression(String expression, JsonDataType dataType);

    Optional<Object> staticEvaluateExpression(ValueExpression expression);

    Object evaluateWithContext(ExpressionValue expressionValue);
}
