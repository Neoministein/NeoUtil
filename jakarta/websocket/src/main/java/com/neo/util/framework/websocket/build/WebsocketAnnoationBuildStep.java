package com.neo.util.framework.websocket.build;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.websocket.api.NeoUtilWebsocket;
import com.neo.util.framework.websocket.api.WebserverHttpHeaderForwarding;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates the method signature of {@link NeoUtilWebsocket}
 */
public class WebsocketAnnoationBuildStep implements BuildStep {

    private static final ExceptionDetails EX_MISSING_SERVER_ENDPOINT = new ExceptionDetails("compile/websocket/missing/serverEndpoint",
            "The Class [{0}] requires the ServerEndpoint annotation and configurator [{1}]");

    private static final ExceptionDetails EX_MASSING_PARAMETER = new ExceptionDetails("compile/websocket/missing/parameter",
            "The method [{0}.{1}] requires the parameters {2}");

    private static final ExceptionDetails EX_MASSING_METHOD = new ExceptionDetails("compile/websocket/missing/method",
            "The class [{0}] requires a method annotated with [{1}]");

    @Override
    public void execute(BuildContext context) {
        for (Class<?> clazz: context.fullReflection().getClassesByAnnotation(NeoUtilWebsocket.class)) {
            validateWebsocketClass(clazz);
        }
    }

    protected void validateWebsocketClass(Class<?> clazz) {
        ServerEndpoint serverEndpoint = clazz.getAnnotation(ServerEndpoint.class);
        if (serverEndpoint == null || !serverEndpoint.configurator().isAssignableFrom(WebserverHttpHeaderForwarding.class)) {
            throw new ConfigurationException(EX_MISSING_SERVER_ENDPOINT, clazz.getName(), WebserverHttpHeaderForwarding.class.getName());
        }

        boolean onOpen = false;
        boolean onMessage = false;
        boolean onClose = false;

        for (Method method: clazz.getMethods()) {
            Set<Class<? extends Annotation>> annotations = Arrays.stream(method.getAnnotations()).map(Annotation::annotationType).collect(Collectors.toUnmodifiableSet());
            if (annotations.contains(OnOpen.class)) {
                validateMethod(method, Session.class, EndpointConfig.class);
                onOpen = true;
            } else if (annotations.contains(OnMessage.class)) {
                validateMethod(method, Session.class);
                onMessage = true;
            } else if (annotations.contains(OnClose.class)) {
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

    protected void validateMethod(Method method, Class<?>... requiresParameters) {
        List<String> requiresParameters2 = Arrays.stream(requiresParameters).map(Class::getName).collect(Collectors.toList());
        for (Parameter parameter : method.getParameters()) {
            requiresParameters2.remove(parameter.getType().getName());
        }

        if (!requiresParameters2.isEmpty()) {
            throw new ConfigurationException(EX_MASSING_PARAMETER, method.getDeclaringClass().getName(), method.getName(), Arrays.toString(requiresParameters));
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}
