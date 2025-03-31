package com.neo.util.framework.mapping.impl;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.node.ExpressionNode;
import com.neo.util.framework.mapping.api.node.Node;
import com.neo.util.framework.mapping.api.node.StaticNode;
import jakarta.el.ExpressionFactory;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;

import java.util.Optional;

public class ElExpressionHandler implements ExpressionHandler {

    private final ExpressionFactory factory;
    private final StandardELContext staticContext;
    private final StandardELContext mappingContext;

    public ElExpressionHandler(StandardELContext mappingContext)  {
        factory = ExpressionFactory.newInstance();
        staticContext = new StandardELContext(factory);
        this.mappingContext = mappingContext;
    }

    public Node parseSingleValue(String fieldName, String expression, String defaultValue, JsonDataType dataType) {
        Class<?> singleValueClass = classFromDataType(dataType);

        ValueExpression elExpression = createExpression(expression, singleValueClass);
        Optional<Object> evaluatedElExpression = staticEvaluateExpression(elExpression);

        if (evaluatedElExpression.isPresent()) {
            return new StaticNode(dataType, fieldName, parseDataType(evaluatedElExpression.get().toString(), dataType));
        }

        if (defaultValue != null) {
            ValueExpression defaultExpression = createExpression(defaultValue, singleValueClass);
            Optional<Object> defaultEvaluatedExpression = staticEvaluateExpression(defaultExpression);
            if (defaultEvaluatedExpression.isEmpty()) {
                throw new RuntimeException(); //TODO
            }
            return new ExpressionNode(dataType, fieldName, new ElExpressionWrapper(elExpression), parseDataType(defaultEvaluatedExpression.get().toString(), dataType));
        }

        return new ExpressionNode(dataType, fieldName, new ElExpressionWrapper(elExpression), null);
    }

    private Object parseDataType(String value, JsonDataType dataType) {
        return switch (dataType) {
            case STRING -> value;
            case INTEGER -> Integer.parseInt(value);
            case NUMBER -> Float.parseFloat(value);
            case BOOLEAN -> Boolean.parseBoolean(value);
            case ARRAY -> throw new RuntimeException(); //TODO
            case OBJECT -> throw new RuntimeException(); //TODO
        };
    }

    private Class<?> classFromDataType(JsonDataType dataType) {
        return switch (dataType) {
            case STRING -> String.class;
            case INTEGER -> Integer.class;
            case NUMBER -> Float.class;
            case BOOLEAN -> Boolean.class;
            case ARRAY -> throw new RuntimeException(); //TODO
            case OBJECT -> throw new RuntimeException(); //TODO
        };
    }


    @Override
    public ValueExpression createExpression(String expression, Class<?> resultType) {
        return factory.createValueExpression(staticContext, expression, resultType);
    }

    @Override
    public Optional<Object> staticEvaluateExpression(ValueExpression expression) {
        try {
            return Optional.ofNullable(expression.getValue(staticContext));
        }  catch (PropertyNotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Object evaluateWithContext(ExpressionNode expressionNode) {
        if (expressionNode.getExpression() instanceof ElExpressionWrapper elWrapper) {
            return elWrapper.expression().getValue(mappingContext);
        }
        throw new RuntimeException("");
    }
}
