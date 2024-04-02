package com.neo.util.framework.websocket.impl.interceptor;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.websocket.api.WebsocketInterceptorLogic;
import com.neo.util.framework.websocket.impl.interceptor.internal.NeoUtilWebsocketOnOpen;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

@NeoUtilWebsocketOnOpen
@Dependent
@Interceptor
@Priority(PriorityConstants.PLATFORM_BEFORE)
public class WebsocketOnOpenInterceptor {

    protected final WebsocketInterceptorLogic interceptorLogic;

    @Inject
    public WebsocketOnOpenInterceptor(WebsocketInterceptorLogic interceptorLogic) {
        this.interceptorLogic = interceptorLogic;
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        Session session = null;
        EndpointConfig config = null;

        for (Object o: invocationContext.getParameters()) {
            if (o instanceof Session x) {
                session = x;
            } else if (o instanceof EndpointConfig x) {
                config = x;
            }
        }

        interceptorLogic.onOpen(invocationContext, session, config);
        return null;
    }
}
