package com.neo.util.framework.websocket.api.monitoring;


import com.neo.util.framework.websocket.persistence.SocketLogSearchable;

import java.util.Collection;

public interface MonitorableWebsocket {

    Collection<SocketLogSearchable> getSocketData();

    void clearSocketData();
}
