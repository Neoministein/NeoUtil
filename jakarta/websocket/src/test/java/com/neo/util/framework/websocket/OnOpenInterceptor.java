package com.neo.util.framework.websocket;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.jboss.weld.context.bound.BoundSessionContext;

import java.io.Serializable;
import java.util.HashMap;

@InSession
@Interceptor
@Priority(100)
public class OnOpenInterceptor implements Serializable {


    @Inject
    BoundSessionContext boundSessionContext;


    @AroundInvoke
    public Object manage(InvocationContext ic) throws Exception {
        //Session session = (Session) ic.getParameters()[0];

        HashMap<String, Object> contextMap = new HashMap<String, Object>();
        boundSessionContext.associate(contextMap);
        boundSessionContext.activate();
        Object o = ic.proceed();
        boundSessionContext.invalidate();
        boundSessionContext.deactivate();
        boundSessionContext.dissociate(contextMap);
        return o;
    }
}
