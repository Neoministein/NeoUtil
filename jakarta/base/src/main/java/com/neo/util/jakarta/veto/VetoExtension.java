package com.neo.util.jakarta.veto;

import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class VetoExtension implements Extension {

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {

        var type = event.getAnnotatedType();

        var requires = type.getAnnotation(RequiresBean.class);

        if (requires == null) {
            return;
        }

        for (Class<?> clazz: requires.value()) {
            if (!hasImplementation(clazz)) {
                event.veto();
                return;
            }
        }
    }

    public boolean hasImplementation(Class<?> clazz) {
        return !IndexReflectionProvider.INSTANCE.getSubTypesOf(clazz).isEmpty();
    }
}
