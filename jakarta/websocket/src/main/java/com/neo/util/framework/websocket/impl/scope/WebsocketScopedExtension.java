package com.neo.util.framework.websocket.impl.scope;

import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.api.scope.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.scope.WebsocketScope;
import com.neo.util.framework.websocket.api.scope.internal.NeoUtilWebsocketOnClose;
import com.neo.util.framework.websocket.api.scope.internal.NeoUtilWebsocketOnMessage;
import com.neo.util.framework.websocket.api.scope.internal.NeoUtilWebsocketOnOpen;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedParameterConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WebsocketScopedExtension implements Extension {

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        bbd.addScope(WebsocketScope.class, true, false);
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        abd.addContext(new ScopeContext<>(WebsocketScope.class));
    }

    public void processAnnotatedType(@Observes ProcessAnnotatedType<?> pat) {
        Class<?> clazz = pat.getAnnotatedType().getJavaClass();
        if (clazz.getAnnotation(NeoUtilWebsocket.class) != null) {
            ServerEndpoint serverEndpoint = clazz.getAnnotation(ServerEndpoint.class);
            if (serverEndpoint == null) {
                throw new IllegalStateException();
            }

            if (!serverEndpoint.configurator().isAssignableFrom(WebserverHttpHeaderForwarding.class))  {
                throw new IllegalStateException();
            }


            boolean onOpen = false;
            boolean onMessage = false;
            boolean onClose = false;

            for (AnnotatedMethodConfigurator<?> method: pat.configureAnnotatedType().methods()) {
                Set<Class<? extends Annotation>> annotations = method.getAnnotated().getAnnotations().stream().map(Annotation::annotationType).collect(Collectors.toUnmodifiableSet());
                if (annotations.contains(OnOpen.class)) {
                    method.add(() -> NeoUtilWebsocketOnOpen.class);
                    validateMethod(method, Session.class, EndpointConfig.class);
                    onOpen = true;
                } else if (annotations.contains(OnMessage.class)) {
                    method.add(() -> NeoUtilWebsocketOnMessage.class);
                    validateMethod(method, Session.class);
                    onMessage = true;
                } else if (annotations.contains(OnClose.class)) {
                    method.add(() -> NeoUtilWebsocketOnClose.class);
                    validateMethod(method, Session.class);
                    onClose = true;
                } else if (onOpen && onMessage && onClose) {
                    break;
                }
            }

            if (!onClose) {
                throw new IllegalStateException();
            }

            if (!onOpen) {
                throw new IllegalStateException();
            }
        }
    }

    protected void validateMethod(AnnotatedMethodConfigurator<?> methodConfigurator, Class<?>... requiresParameters) {
        List<String> requiresParameters2 = Arrays.stream(requiresParameters).map(Class::getName).collect(Collectors.toList());
        for (AnnotatedParameterConfigurator<?> value : methodConfigurator.params()) {
            requiresParameters2.remove(value.getAnnotated().getBaseType().getTypeName());
        }

        if (!requiresParameters2.isEmpty()) {
            throw new IllegalStateException();
        }

        //methodConfigurator.params().get(0).getClass()
    }
}