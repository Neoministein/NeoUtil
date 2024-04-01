package com.neo.util.framework.websocket.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.websocket.api.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import com.neo.util.framework.websocket.api.WebsocketScope;
import com.neo.util.framework.websocket.impl.scope.ScopeContext;
import com.neo.util.framework.websocket.impl.scope.internal.NeoUtilWebsocketOnClose;
import com.neo.util.framework.websocket.impl.scope.internal.NeoUtilWebsocketOnMessage;
import com.neo.util.framework.websocket.impl.scope.internal.NeoUtilWebsocketOnOpen;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedParameterConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WebsocketScopedExtension implements Extension {

    private static final ExceptionDetails EX_MISSING_SERVER_ENDPOINT = new ExceptionDetails("websocket/missing/serverEndpoint",
            "The Class [{0}] requires the ServerEndpoint annotation and configurator [{1}]");

    private static final ExceptionDetails EX_MASSING_PARAMETER = new ExceptionDetails("websocket/missing/parameter",
            "The method [{0}.{1}] requires the parameters {2}");

    private static final ExceptionDetails EX_MASSING_METHOD = new ExceptionDetails("websocket/missing/method",
            "The class [{0}] requires a method annotated with [{1}]");

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        bbd.addScope(WebsocketScope.class, true, false);
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        abd.addContext(new ScopeContext<>(WebsocketScope.class));
    }

    public void processAnnotatedType(@Observes @WithAnnotations(NeoUtilWebsocket.class) ProcessAnnotatedType<?> pat) {
        Class<?> clazz = pat.getAnnotatedType().getJavaClass();
        if (clazz.getAnnotation(NeoUtilWebsocket.class) != null) {
            ServerEndpoint serverEndpoint = clazz.getAnnotation(ServerEndpoint.class);
            if (serverEndpoint == null || !serverEndpoint.configurator().isAssignableFrom(WebserverHttpHeaderForwarding.class)) {
                throw new ConfigurationException(EX_MISSING_SERVER_ENDPOINT, clazz.getName(), WebserverHttpHeaderForwarding.class.getName());
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
                throw new ConfigurationException(EX_MASSING_METHOD, clazz.getName(), OnClose.class.getName());
            }

            if (!onOpen) {
                throw new ConfigurationException(EX_MASSING_METHOD, clazz.getName(), OnOpen.class.getName());
            }
        }
    }

    protected void validateMethod(AnnotatedMethodConfigurator<?> methodConfigurator, Class<?>... requiresParameters) {
        List<String> requiresParameters2 = Arrays.stream(requiresParameters).map(Class::getName).collect(Collectors.toList());
        for (AnnotatedParameterConfigurator<?> value : methodConfigurator.params()) {
            requiresParameters2.remove(value.getAnnotated().getBaseType().getTypeName());
        }

        if (!requiresParameters2.isEmpty()) {
            Method method = methodConfigurator.getAnnotated().getJavaMember();
            throw new ConfigurationException(EX_MASSING_PARAMETER, method.getDeclaringClass().getName(), method.getName(), requiresParameters);
        }
    }
}