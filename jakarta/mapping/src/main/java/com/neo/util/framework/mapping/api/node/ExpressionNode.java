package com.neo.util.framework.mapping.api.node;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionValue;

public final class ExpressionNode extends Node {

    private final ExpressionValue expression;
    private final Object defaultValue;

    public ExpressionNode(JsonDataType dataType, String filedName, ExpressionValue expression, Object defaultValue) {
        super(dataType, filedName);
        this.expression = expression;
        this.defaultValue = defaultValue;
    }

    public ExpressionValue getExpression() {
        return expression;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
