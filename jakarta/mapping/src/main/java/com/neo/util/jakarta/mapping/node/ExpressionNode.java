package com.neo.util.jakarta.mapping.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.jakarta.mapping.expression.ExpressionValue;

public final class ExpressionNode extends Node {

    private final ExpressionValue expression;

    public ExpressionNode(JsonDataType dataType, String filedName, ExpressionValue expression, ExpressionValue skipExpression) {
        super(dataType, filedName, skipExpression);
        this.expression = expression;
    }

    public ExpressionValue getExpression() {
        return expression;
    }
}
