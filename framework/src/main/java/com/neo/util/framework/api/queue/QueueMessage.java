package com.neo.util.framework.api.queue;

import com.neo.util.common.impl.json.JsonUtil;

import java.io.Serializable;

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

    /**
     * An arbitrary serializable data structure. Which will be parsed to json.
     */
    protected String message;

    public QueueMessage(String messageType, Serializable payload) {
        this.messageType = messageType;
        this.message = JsonUtil.toJson(payload);
        this.messageClass = payload.getClass().getName();
    }

    public Serializable getPayload() {
        try {
            return (Serializable) JsonUtil.fromJson(message, Class.forName(messageClass));
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
