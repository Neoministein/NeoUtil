package io.helidon.microprofile.tyrus;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.glassfish.tyrus.core.ComponentProvider;

import java.lang.annotation.Annotation;

public class HelidonComponentProvider extends ComponentProvider {
    public HelidonComponentProvider() {
    }

    public boolean isApplicable(Class<?> c) {
        BeanManager beanManager = CDI.current().getBeanManager();
        return beanManager.getBeans(c, new Annotation[0]).size() > 0;
    }

    public <T> Object create(Class<T> c) {
        return CDI.current().select(c, new Annotation[0]).get();
    }

    public boolean destroy(Object o) {
        try {
            CDI.current().destroy(o);
            return true;
        } catch (IllegalStateException | UnsupportedOperationException var3) {
            return false;
        }
    }
}
