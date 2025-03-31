package com.neo.util.framework.mapping.impl;

import com.neo.util.framework.mapping.api.ExpressionValue;
import jakarta.el.ValueExpression;

public class ElExpressionWrapper implements ExpressionValue {

    private final ValueExpression expression;

    public ElExpressionWrapper(ValueExpression expression) {
        this.expression = expression;
    }

    public ValueExpression expression() {
        return expression;
    }
}
