package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public abstract class AbstractMpQueueListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMpQueueListener.class);

    @Inject
    protected RequestContextExecutor requestContextExecutor;

    @Inject
    protected InstanceIdentification instanceIdentification;

    protected abstract QueueListener getListener();

    protected abstract String getQueueName();

    protected CompletionStage<Void> handleMessage(Message<String> msg) {
        QueueMessage queueMessage;
        try {
            queueMessage = JsonUtil.fromJson(msg.getPayload(), QueueMessage.class);
        } catch(ValidationException ex) {
            LOGGER.error("Unable to parse incoming queue message. Action won't be retried.", ex);
            return msg.ack();
        }
        try {
            requestContextExecutor.execute(new QueueRequestDetails(instanceIdentification.getInstanceId(), queueMessage, new QueueRequestDetails.Context(getQueueName())), () -> getListener().onMessage(queueMessage));
            return msg.ack();
        } catch(Exception ex) {
            LOGGER.error("Unexpected error occurred while processing a queue message. Action will be retried based on the retry policy.", ex);
            return msg.nack(ex);
        }
    }
}
