package com.neo.util.framework.api.queue;

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

    /**
     * An arbitrary serializable data structure.
     */
    protected Serializable message;

    public QueueMessage(String messageType, Serializable payload) {
        this.messageType = messageType;
        this.message = payload;
    }

    public QueueMessage(String messageType) {
        this.messageType = messageType;
    }

    public QueueMessage() {}

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Serializable getMessage() {
        return message;
    }

    public void setMessage(Serializable message) {
        this.message = message;
    }
}
