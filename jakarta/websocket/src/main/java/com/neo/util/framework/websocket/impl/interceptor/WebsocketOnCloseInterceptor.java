package com.neo.util.framework.websocket.impl.interceptor;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.websocket.api.WebsocketInterceptorLogic;
import com.neo.util.framework.websocket.impl.interceptor.internal.NeoUtilWebsocketOnClose;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.Session;

@NeoUtilWebsocketOnClose
@Interceptor
@Priority(PriorityConstants.PLATFORM_BEFORE)
public class WebsocketOnCloseInterceptor {

    protected final WebsocketInterceptorLogic interceptorLogic;

    @Inject
    public WebsocketOnCloseInterceptor(WebsocketInterceptorLogic interceptorLogic) {
        this.interceptorLogic = interceptorLogic;
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        Session session = null;

        for (Object o: invocationContext.getParameters()) {
            if (o instanceof Session x) {
                session = x;
                break;
            }
        }

        interceptorLogic.onClose(invocationContext, session);
        return null;
    }
}
