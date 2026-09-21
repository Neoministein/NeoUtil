package com.neo.util.jakarta.mapping.expression;

public class StaticExpressionValue implements ExpressionValue {

    private final Object value;

    public StaticExpressionValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
