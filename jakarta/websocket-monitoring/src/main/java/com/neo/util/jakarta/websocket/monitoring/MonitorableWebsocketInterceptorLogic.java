package com.neo.util.jakarta.websocket.monitoring;

import com.neo.util.api.security.AuthenticationProvider;
import com.neo.util.api.security.HttpCredentialsGenerator;
import com.neo.util.jakarta.request.InstanceIdentification;
import com.neo.util.jakarta.request.RequestContextExecutor;
import com.neo.util.jakarta.websocket.DefaultWebsocketStateContext;
import com.neo.util.jakarta.websocket.WebsocketStateContext;
import com.neo.util.jakarta.websocket.interceptor.WebsocketInterceptorLogicImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Function;

@Specializes
@ApplicationScoped
public class MonitorableWebsocketInterceptorLogic extends WebsocketInterceptorLogicImpl {

    @Inject
    public MonitorableWebsocketInterceptorLogic(RequestContextExecutor executor, InstanceIdentification instanceIdentification, HttpCredentialsGenerator credentialsGenerator, AuthenticationProvider authenticationProvider) {
        super(executor, instanceIdentification, credentialsGenerator, authenticationProvider);
    }

    @Override
    public void onMessage(InvocationContext invocationContext, Session session) throws Exception {
        WebsocketStateContext stateContext = websocketStateContextMap.get(session.getId());
        if (stateContext instanceof MonitorableWebsocketStateContext monitorableContext) {
            monitorableContext.addToIncomingSocketLog(invocationContext);
        }
        super.onMessage(invocationContext, session);
    }

    @Override
    protected WebsocketStateContext createContext(InvocationContext invocationContext, Session session, MultivaluedMap<String, String> headers) {
        DefaultWebsocketStateContext context = (DefaultWebsocketStateContext) super.createContext(invocationContext, session, headers);
        if (isMonitored(invocationContext)) {
            return new MonitorableWebsocketStateContext(context, getMessageFromContext(invocationContext));
        }

        return context;
    }

    protected boolean isMonitored(InvocationContext invocationContext) {
        return invocationContext.getMethod().getDeclaringClass().getAnnotation(MonitoredWebsocket.class) != null;
    }

    /**
     * Finds out at which position the {@link String} is for incoming request and returns it.
     * <p>
     * Could perhaps be cached for performance
     */
    protected Function<InvocationContext, String> getMessageFromContext(InvocationContext invocationContext) {
        for (Method method: invocationContext.getMethod().getDeclaringClass().getMethods()) {
            if (method.getAnnotation(OnMessage.class) != null) {
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> clazz = parameters[i].getType();
                    if (clazz.equals(String.class) && parameters[i].getAnnotations().length == 0) {
                        final int index = i;
                        return context -> (String) context.getParameters()[index];
                    }
                }
            }
        }

        return x -> "";
    }
}
