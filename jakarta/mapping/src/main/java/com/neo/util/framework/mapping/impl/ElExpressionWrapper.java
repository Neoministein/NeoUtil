package com.neo.util.framework.mapping.impl;

import com.neo.util.framework.mapping.api.ExpressionValue;
import jakarta.el.ValueExpression;

public class ElExpressionWrapper implements ExpressionValue {

    private final String name;
    private final ValueExpression expression;

    public ElExpressionWrapper(String name, ValueExpression expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public ValueExpression expression() {
        return expression;
    }
}
