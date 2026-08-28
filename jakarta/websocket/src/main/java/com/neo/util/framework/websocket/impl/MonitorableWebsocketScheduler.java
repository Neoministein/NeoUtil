package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.websocket.api.WebsocketInterceptorLogic;
import com.neo.util.framework.websocket.api.WebsocketStateContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MonitorableWebsocketScheduler {

    protected final SearchProvider searchProvider;
    protected final WebsocketInterceptorLogic websocketInterceptorLogic;

    @Inject
    public MonitorableWebsocketScheduler(SearchProvider searchProvider, WebsocketInterceptorLogic websocketInterceptorLogic) {
        this.searchProvider = searchProvider;
        this.websocketInterceptorLogic = websocketInterceptorLogic;
    }

    @CronSchedule(value = "MonitorableWebsocketScheduler", cron = "0 0/1 * * * *") // Every Minute
    public void action() {
        for (WebsocketStateContext stateContext: websocketInterceptorLogic.getActiveWebsocketStates()) {
            if (stateContext.isMonitored()) {
                searchProvider.index(stateContext.clearSocketLog());
            }
        }
    }
}
