package com.neo.util.framework.mapping.impl;

import com.neo.util.common.api.json.JsonDataType;
import com.neo.util.framework.mapping.api.ExpressionHandler;
import com.neo.util.framework.mapping.api.ExpressionValue;
import jakarta.el.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

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

    @Override
    public ExpressionValue createExpression(String name, String expression, JsonDataType dataType) {
        Class<?> singleValueClass = classFromDataType(dataType);
        ElExpressionWrapper elExpressionWrapper = new ElExpressionWrapper(name, createExpressionInternal(expression, singleValueClass));
        Optional<Object> evaluatedElExpression = staticEvaluateExpression(elExpressionWrapper);
        if (evaluatedElExpression.isPresent()) {
            return new StaticExpressionValue(evaluatedElExpression.get());
        }
        return elExpressionWrapper;
    }

    private Class<?> classFromDataType(JsonDataType dataType) {
        return switch (dataType) {
            case STRING -> String.class;
            case INTEGER -> Integer.class;
            case NUMBER -> Float.class;
            case BOOLEAN -> Boolean.class;
            case ARRAY, OBJECT -> Object.class;
        };
    }

    public ValueExpression createExpressionInternal(String expression, Class<?> resultType) {
        return factory.createValueExpression(staticContext, expression, resultType);
    }

    @Override
    public Optional<Object> staticEvaluateExpression(ExpressionValue expression) {
        try {
            return Optional.ofNullable(evaluate(expression, staticContext));
        } catch (IllegalArgumentException ex) {
            if (ex.getCause() instanceof PropertyNotFoundException) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @Override
    public Object evaluateWithContext(ExpressionValue expressionNode) {
        return evaluate(expressionNode, mappingContext);
    }

    protected Object evaluate(ExpressionValue expressionNode, ELContext context) {
        if (expressionNode instanceof ElExpressionWrapper elWrapper) {
            try {
                return elWrapper.expression().getValue(context);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to evaludate field: [" + elWrapper.getName() + "], error: [" + ex.getMessage() + "]", ex);
            }

        } else if (expressionNode instanceof StaticExpressionValue staticExpressionValue) {
            return staticExpressionValue.getValue();
        }
        throw new IllegalArgumentException("The ExpressionValue Class [" + expressionNode.getClass().getName() + "] has not been implemented");
    }
}
