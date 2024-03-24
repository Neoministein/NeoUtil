package com.neo.util.framework.websocket.impl.interceptor;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.websocket.api.scope.WebsocketScope;
import com.neo.util.framework.websocket.api.scope.internal.NeoUtilWebsocketOnMessage;
import com.neo.util.framework.websocket.impl.WebsocketStateHolder;
import com.neo.util.framework.websocket.impl.scope.ScopeContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.Session;

@NeoUtilWebsocketOnMessage
@Interceptor
@Priority(PriorityConstants.PLATFORM_BEFORE)
public class WebsocketOnMessageInterceptor {

    protected final BeanManager beanManager;
    protected final RequestContextExecutor executor;
    protected final WebsocketStateHolder sessionHolder;

    @Inject
    public WebsocketOnMessageInterceptor(BeanManager beanManager, RequestContextExecutor executor, WebsocketStateHolder sessionHolder) {
        this.beanManager = beanManager;
        this.executor = executor;
        this.sessionHolder = sessionHolder;
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

        if (session == null) {
            throw new IllegalStateException();
        }
        logic(invocationContext, session);
        return null;
    }

    public void logic(InvocationContext invocationContext, Session session) throws Exception {
        ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(WebsocketScope.class);
        context.enter(session.getId());

        try {
            executor.executeChecked(sessionHolder.getRequestDetails(), invocationContext::proceed);
        } finally {
            context.exit(session.getId());
        }
    }
}
