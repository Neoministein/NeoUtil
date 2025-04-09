package com.neo.util.framework.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

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
            MappingStateHolder stateHolder = lookupStateHolder();
            if ("_iteration".equals(path)) {
                Optional<Integer> iteration = stateHolder.getLoopIteration(base);
                if (iteration.isPresent()) {
                    context.setPropertyResolved(true);
                    return iteration.get();
                }
            }

            if (base instanceof JsonNode node) {
                context.setPropertyResolved(true);
                return node.path(path);
            }

            Optional<Object> mappingNode = stateHolder.getValueFromInput(path);
            if (mappingNode.isPresent()) {
                context.setPropertyResolved(true);
                return mappingNode.get();
            }
        }

        return wrappedResolver.getValue(context, base, property);
    }

    @Override
    public <T> T convertToType(ELContext context, Object obj, Class<T> targetType) {
        if (obj instanceof JsonNode node) {
            if (targetType.equals(String.class)) {
                context.setPropertyResolved(true);
                return (T)node.textValue();
            } else if (targetType.equals(Integer.class)) {
                context.setPropertyResolved(true);
                return (T) (Object) node.longValue();
            } else if (targetType.equals(Float.class)) {
                context.setPropertyResolved(true);
                return (T) (Object) node.doubleValue();
            } else if (targetType.equals(Boolean.class)) {
                context.setPropertyResolved(true);
                return (T) (Object) node.booleanValue();
            }
        }

        return super.convertToType(context, obj, targetType);
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
}
