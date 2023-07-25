package com.neo.util.framework.api.queue;

import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.request.RequestDetails;

import java.io.Serializable;
import java.util.*;

/**
 * The Queue Message
 * <p>
 * A queue message contains a unique messageType and the message itself. The message can be any arbitrary data structure
 * which can be used to supply information depending on the message type.
 * <p>
 * Listeners can subscribe to the topic or queue and execute arbitrary actions based on the message they receive.
 */
public class QueueMessage implements Serializable {

    protected String initiator;

    protected String traceId;

    /**
     * The type of the queue message.
     */
    protected String messageType;

    protected String messageClass;

    protected String collectionClass = null;

    /**
     * An arbitrary serializable data structure. Which will be parsed to json.
     */
    protected String message;

    public QueueMessage(RequestDetails requestDetails, String messageType, Serializable payload) {
        this(requestDetails.getInitiator(), requestDetails.getTraceId(), messageType, payload);
    }

    public QueueMessage(String initiator, String traceId, String messageType, Serializable payload) {
        this.initiator = initiator;
        this.traceId = traceId;
        this.messageType = messageType;
        this.message = JsonUtil.toJson(payload);

        if (payload == null) {
            throw new NullPointerException("QueueMessage payload cannot be null");
        } else if (payload instanceof List<?> list) {
            this.messageClass = list.iterator().next().getClass().arrayType().getName();
            this.collectionClass = "LIST";
        } else if (payload instanceof Set<?> set) {
            this.messageClass = set.iterator().next().getClass().arrayType().getName();
            this.collectionClass = "SET";
        } else {
            this.messageClass = payload.getClass().getName();
        }
    }

    protected QueueMessage() {
        //Required by Jackson
    }

    public Serializable getPayload() {
        try {
            Serializable serializedPayload = (Serializable) JsonUtil.fromJson(message, Class.forName(messageClass));
            if ("SET".equals(collectionClass)) {
                return new HashSet<>(Arrays.asList((Serializable[]) serializedPayload));
            } else if ("LIST".equals(collectionClass)) {
                return new ArrayList<>(Arrays.asList((Serializable[]) serializedPayload));
            } else {
                return serializedPayload;
            }
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getTraceId() {
        return traceId;
    }
}