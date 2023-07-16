package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueProducer;
import com.neo.util.framework.api.queue.QueueService;
import com.neo.util.framework.api.request.RequestDetails;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Priority(PriorityConstants.APPLICATION)
@Alternative
@ApplicationScoped
public class MicroProfileQueueService implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroProfileQueueService.class);

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    protected Map<String, QueueProducer> queueProducerMap = new HashMap<>();

    /**
     * Initializes the mapping to the {@link QueueProducer}.
     * This is done only once at startup as no new queues should be added at runtime.
     */
    @Inject
    protected void init(Instance<QueueProducer> queueProducerInstances) {
        for (QueueProducer queueProducer: queueProducerInstances) {
            if (queueProducerMap.containsKey(queueProducer.getQueueName())) {
                throw new ConfigurationException(QueueService.EX_DUPLICATED_QUEUE,
                        queueProducer.getClass().getName(), queueProducerMap.get(queueProducer.getClass().getName()));
            } else {
                queueProducerMap.put(queueProducer.getQueueName(), queueProducer);
                LOGGER.info("Registered queue {}", queueProducer.getQueueName());
            }

        }
    }

    protected void onStartUp(@Observes ApplicationPreReadyEvent preReadyEvent) {
        LOGGER.debug("Startup event received");
    }

    @Override
    public void addToQueue(String queueName, Serializable payload) {
        addToQueue(queueName, new QueueMessage(requestDetailsProvider.get(), "", payload));
    }

    @Override
    public void addToQueue(String queueName, QueueMessage message) {
        queueProducerMap.computeIfAbsent(queueName, s -> {
            throw new ConfigurationException(QueueService.EX_NON_EXISTENT_QUEUE, QueueProducer.class.getSimpleName(), queueName); })
                .addToQueue(JsonUtil.toJson(message));
    }
}
