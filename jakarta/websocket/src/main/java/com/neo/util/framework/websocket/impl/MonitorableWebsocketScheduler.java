package com.neo.util.framework.websocket.impl;

import com.neo.util.api.persistence.search.SearchProvider;
import com.neo.util.api.scheduler.CronSchedule;
import com.neo.util.framework.websocket.api.WebsocketInterceptorLogic;
import com.neo.util.framework.websocket.api.WebsocketStateContext;
import com.neo.util.jakarta.veto.RequiresBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@RequiresBean(SearchProvider.class)
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
