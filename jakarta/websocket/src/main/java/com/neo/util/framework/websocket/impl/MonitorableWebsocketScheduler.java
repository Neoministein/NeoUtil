package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import com.neo.util.framework.websocket.api.WebsocketInterceptorLogic;
import com.neo.util.framework.websocket.api.WebsocketStateContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MonitorableWebsocketScheduler {

    protected final SearchProvider searchProvider;
    protected final WebsocketInterceptorLogic websocketInterceptorLogic;

    @Inject
    public MonitorableWebsocketScheduler(SearchProvider searchProvider, WebsocketInterceptorLogic websocketInterceptorLogic) {
        this.searchProvider = searchProvider;
        this.websocketInterceptorLogic = websocketInterceptorLogic;
    }

    @FixedRateSchedule(value = "MonitorableWebsocketScheduler", delay = 1, timeUnit = TimeUnit.MINUTES)
    public void action() {
        for (WebsocketStateContext websocketStateHolder: websocketInterceptorLogic.getActiveWebsocketStates()) {
            if (websocketStateHolder.isMonitored()) {
                searchProvider.index(websocketStateHolder.clearSocketLog());
            }
        }
    }
}
