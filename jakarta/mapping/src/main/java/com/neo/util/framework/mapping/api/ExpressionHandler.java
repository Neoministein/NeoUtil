package com.neo.util.framework.mapping.api;

import com.neo.util.common.api.json.JsonDataType;

import java.util.Optional;

/**
 * Handles the creation and evaluation of expression
 */
public interface ExpressionHandler {

    /**
     * Creates an expression based on the parameters
     *
     * @param name identifier of the expression
     * @param expression the expression itself
     * @param dataType the return datatype of the expression
     *
     * @return create ExpressionValue object
     */
    ExpressionValue createExpression(String name, String expression, JsonDataType dataType);

    /**
     * Evaluates the expression with no context of the surrounding application.
     *
     * @param expression to evaluate
     * @return optional result of the expression.
     */
    Optional<Object> staticEvaluateExpression(ExpressionValue expression);

    /**
     * Evaluates the expression with context of the entire application.
     *
     * @param expression to evaluate
     * @return result of the expression.
     */
    Object evaluateWithContext(ExpressionValue expression);
}
