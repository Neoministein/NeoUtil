package com.neo.util.framework.websocket.impl.monitoring;

import com.neo.util.framework.websocket.api.monitoring.MonitorableWebsocket;
import com.neo.util.framework.websocket.impl.AbstractWebsocketEndpoint;
import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractMonitorableWebsocket extends AbstractWebsocketEndpoint implements MonitorableWebsocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMonitorableWebsocket.class);

    protected Map<Session, SocketLogSearchable> socketData = new ConcurrentHashMap<>();

    public Collection<SocketLogSearchable> getSocketData() {
        return socketData.values();
    }

    public void clearSocketData() {
        socketData.clear();
    }

    @Override
    public void onMessage(Session session, String message) {
        modifySearchableData(session, val -> val.addToIncoming(message.length()));
    }

    @SuppressWarnings("java:S2445") //Based on oracle guide
    protected void broadcast(Session session, String message) {
        modifySearchableData(session, val -> val.addToOutgoing(message.length()));

        synchronized (session) {
            try {
               session.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                LOGGER.warn("Unable to broadcast message [{}] to session [{}]", ex.getMessage(), session.getId());
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Provided message for session [{}] is null", session.getId());
            }
        }
    }

    protected void modifySearchableData(Session session, Consumer<SocketLogSearchable> edit) {
        edit.accept(socketData.computeIfAbsent(session, SocketLogSearchable::new));
    }
}
