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
        if (base instanceof JsonNode node) {
            context.setPropertyResolved(true);
            return node.path(property.toString());
        }

        MappingStateHolder mappingStateHolder = lookup();
        Optional<JsonNode> mappingNode = mappingStateHolder.getValueFromMapping(property.toString());
        if (mappingNode.isPresent()) {
            context.setPropertyResolved(true);
            return mappingNode.get();
        }

        return wrappedResolver.getValue(context, base, property);
    }

    private MappingStateHolder lookup() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(MappingStateHolder.class));
        if (bean == null) {
            return null;
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
