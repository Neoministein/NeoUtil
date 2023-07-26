package com.neo.util.framework.websocket.impl.monitoring;

import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import com.neo.util.framework.websocket.api.monitoring.MonitorableWebsocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MonitorableWebsocketScheduler {

    @Inject
    protected Instance<MonitorableWebsocket> monitorableWebsocketList;

    @Inject
    protected SearchProvider searchProvider;

    @FixedRateSchedule(value = "MonitorableWebsocketScheduler", delay = 1, timeUnit = TimeUnit.MINUTES)
    public void action() {
        for (MonitorableWebsocket monitorableWebsocket: monitorableWebsocketList) {
            searchProvider.index( monitorableWebsocket.getSocketData());
            monitorableWebsocket.clearSocketData();
        }
    }
}
