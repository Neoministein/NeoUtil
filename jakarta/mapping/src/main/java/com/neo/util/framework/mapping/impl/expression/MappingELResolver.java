package com.neo.util.framework.mapping.impl.expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.framework.mapping.impl.MappingStateHolder;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Optional;


public class MappingELResolver extends ELResolver {

    private final BeanManager beanManager;
    private final ELResolver wrappedResolver;

    public MappingELResolver(BeanManager beanManager) {
        this.beanManager = beanManager;
        this.wrappedResolver = beanManager.getELResolver();
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (property instanceof String path) {

            if ("_in".equals(path)) {
                context.setPropertyResolved(true);
                return lookupStateHolder();
            }

            if ("_iteration".equals(path)) {
                Optional<Integer> iteration = lookupStateHolder().getLoopIteration(base);
                if (iteration.isPresent()) {
                    context.setPropertyResolved(true);
                    return iteration.get();
                }
            }

            if (base instanceof JsonNode node) {
                context.setPropertyResolved(true);
                JsonNode desiredProperty = node.path(path);
                return tryHandleLeaf(desiredProperty);
            }

            if (base instanceof MappingStateHolder stateHolder) {
                context.setPropertyResolved(true);
                return tryHandleLeaf(stateHolder.getValueFromInput(path));
            }
        }

        return wrappedResolver.getValue(context, base, property);
    }

    @Override
    public <T> T convertToType(ELContext context, Object obj, Class<T> targetType) {
        if (obj == null) {
            context.setPropertyResolved(true);
            return null;
        }

        return super.convertToType(context, obj, targetType);
    }

    private Object tryHandleLeaf(Object value) {
        if (value instanceof JsonNode node) {
            if (node.isTextual()) {
                return node.textValue();
            } else if (node.isInt()) {
                return node.intValue();
            } else if (node.isLong()) {
                return node.longValue();
            } else if (node.isDouble()) {
                return node.doubleValue();
            } else if (node.isBoolean()) {
                return node.booleanValue();
            } else if (node.isMissingNode()) {
                return null;
            }
        }
        return value;
    }

    private MappingStateHolder lookupStateHolder() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(MappingStateHolder.class));
        if (bean == null) {
            throw new RuntimeException();
        }
        return (MappingStateHolder) beanManager.getReference(bean, MappingStateHolder.class, beanManager.createCreationalContext(bean));
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return wrappedResolver.getType(context, base, property);
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        wrappedResolver.setValue(context, base, property, value);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return wrappedResolver.isReadOnly(context, base, property);
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return wrappedResolver.getCommonPropertyType(context, base);
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        return wrappedResolver.invoke(context, base, method, paramTypes, params);
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return wrappedResolver.getFeatureDescriptors(context, base);
    }
}
