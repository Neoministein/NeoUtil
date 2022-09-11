package com.neo.util.framework.api.queue;

import com.neo.util.common.impl.json.JsonUtil;

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

    public QueueMessage(String messageType, Serializable payload) {
        this.messageType = messageType;
        this.message = JsonUtil.toJson(payload);

        switch (payload) {
        case List<?> list -> {
            this.messageClass = list.iterator().next().getClass().arrayType().getName();
            this.collectionClass = "LIST";
        }
        case Set<?> set -> {
            this.messageClass = set.iterator().next().getClass().arrayType().getName();
            this.collectionClass = "SET";
        }
        case null -> throw new NullPointerException("QueueMessage payload cannot be null");
        default -> this.messageClass = payload.getClass().getName();
        }
    }

    protected QueueMessage() {
        //Required by Jackson
    }

    public Serializable getPayload() {
        try {
            Serializable serializedPayload = (Serializable) JsonUtil.fromJson(message, Class.forName(messageClass));
            return switch (collectionClass) {
                case "SET" -> new HashSet<>(Arrays.asList((Serializable[]) serializedPayload));
                case "LIST" -> new ArrayList<>(Arrays.asList((Serializable[]) serializedPayload));
                case null, default -> serializedPayload;
            };
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
}