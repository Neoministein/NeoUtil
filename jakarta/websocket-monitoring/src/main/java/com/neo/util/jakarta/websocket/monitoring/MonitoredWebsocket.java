package com.neo.util.jakarta.websocket.monitoring;


import com.neo.util.jakarta.websocket.WebsocketStateContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the endpoint throughput is monitored with a {@link SocketLogSearchable}
 * <p>
 * Outgoing needs to go through the {@link WebsocketStateContext#broadcastAsync(String)} or {@link WebsocketStateContext#broadcast(String)}
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitoredWebsocket {

}
