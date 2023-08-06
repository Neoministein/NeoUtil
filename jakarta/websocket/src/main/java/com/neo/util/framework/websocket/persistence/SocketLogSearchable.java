package com.neo.util.framework.websocket.persistence;

import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.percistence.request.AbstractLogSearchable;
import com.neo.util.framework.websocket.api.WebsocketRequestDetails;

import java.time.Instant;

@SearchableIndex(indexName = SocketLogSearchable.INDEX_NAME, indexPeriod = IndexPeriod.MONTHLY)
public class SocketLogSearchable extends AbstractLogSearchable {

    public static final String INDEX_NAME = INDEX_PREFIX + "-socket";

    protected String initiator;
    protected String context;
    protected String remoteAddress;
    protected long incoming = 0;
    protected long outgoing = 0;


    public SocketLogSearchable(WebsocketRequestDetails requestDetails) {
        super(requestDetails);
        this.timestamp = Instant.now();
        this.initiator = requestDetails.getInitiator();
        this.context = requestDetails.getRequestContext().toString();
        this.remoteAddress = requestDetails.getRemoteAddress();
    }

    //Required for Jackson
    protected SocketLogSearchable(){}

    public void addToIncoming(long toAdd) {
        incoming += toAdd;
    }

    public void addToOutgoing(long toAdd) {
        outgoing += toAdd;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getContext() {
        return context;
    }

    public long getIncoming() {
        return incoming;
    }

    public long getOutgoing() {
        return outgoing;
    }
}
