package com.neo.util.framework.mapping.impl.expression;

import com.neo.util.framework.mapping.api.ExpressionValue;

public class StaticExpressionValue implements ExpressionValue {

    private final Object value;

    public StaticExpressionValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
