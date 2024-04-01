package com.neo.util.framework.websocket.impl;

import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import com.neo.util.framework.websocket.api.WebsocketStateHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MonitorableWebsocketScheduler {

    protected final SearchProvider searchProvider;
    protected final Instance<WebsocketStateHolder> websocketStateHolders;

    @Inject
    public MonitorableWebsocketScheduler(SearchProvider searchProvider, Instance<WebsocketStateHolder> websocketStateHolders) {
        this.searchProvider = searchProvider;
        this.websocketStateHolders = websocketStateHolders;
    }

    @FixedRateSchedule(value = "MonitorableWebsocketScheduler", delay = 1, timeUnit = TimeUnit.MINUTES)
    public void action() {
        for (WebsocketStateHolder websocketStateHolder: websocketStateHolders) {
            if (websocketStateHolder.isMonitored()) {
                searchProvider.index(websocketStateHolder.clearSocketLog());
            }
        }
    }
}
