package com.neo.util.framework.mapping.impl;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.ExpressionValue;
import com.neo.util.framework.mapping.api.node.ExpressionNode;
import com.neo.util.framework.mapping.api.node.LoopArrayNode;
import com.neo.util.framework.mapping.api.node.Node;
import com.neo.util.framework.mapping.api.node.StaticNode;
import jakarta.el.ExpressionFactory;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ElExpressionHandler implements ExpressionHandler {

    private final ExpressionFactory factory;
    private final StandardELContext staticContext;
    private final StandardELContext mappingContext;


    @Inject
    public ElExpressionHandler(BeanManager beanManager) {
        this.factory = ExpressionFactory.newInstance();
        this.staticContext = new StandardELContext(factory);
        this.mappingContext = new StandardELContext(factory);
        this.mappingContext.putContext(BeanManager.class, beanManager);
        this.mappingContext.addELResolver(new MappingELResolver(beanManager));
    }

    public ExpressionValue createExpression(String expression, JsonDataType dataType) {
        Class<?> singleValueClass = classFromDataType(dataType);
        ValueExpression elExpression = createExpressionInteral(expression, singleValueClass);
        Optional<Object> evaluatedElExpression = staticEvaluateExpression(elExpression);
        if (evaluatedElExpression.isPresent()) {
            return new StaticExpressionValue(evaluatedElExpression.get());
        }
        return new ElExpressionWrapper(elExpression);
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

    private Class<?> classFromDataType(JsonDataType dataType) {//TODO rerfence same as in resolver
        return switch (dataType) {
            case STRING -> String.class;
            case INTEGER -> Integer.class;
            case NUMBER -> Float.class;
            case BOOLEAN -> Boolean.class;
            case ARRAY -> Object.class;
            case OBJECT -> Object.class;
        };
    }

    public ValueExpression createExpressionInteral(String expression, Class<?> resultType) {
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
    public Object evaluateWithContext(ExpressionValue expressionNode) {
        if (expressionNode instanceof ElExpressionWrapper elWrapper) {
            return elWrapper.expression().getValue(mappingContext);
        } else if (expressionNode instanceof StaticExpressionValue staticExpressionValue) {
            return staticExpressionValue.getValue();
        }
        throw new RuntimeException("");
    }
}
