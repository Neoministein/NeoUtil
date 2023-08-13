package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.queue.*;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.ReflectionService;
import com.neo.util.framework.microprofile.reactive.messaging.api.MicroProfileQueueConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Priority(PriorityConstants.APPLICATION)
@Alternative
@ApplicationScoped
public class MicroProfileQueueService implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroProfileQueueService.class);

    @Inject
    protected ConfigService configService;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    protected Map<String, MicroProfileQueueConfig> queueProducerMap;

    /**
     * Initializes the mapping to the {@link QueueProducer}.
     * This is done only once at startup as no new queues should be added at runtime.
     */
    @Inject
    protected void init(Instance<QueueProducer> queueProducerInstances, ReflectionService reflectionService) {
        Map<String, OutgoingQueue> queueConnectionMap = new HashMap<>();
        for (AnnotatedElement annotatedElement: reflectionService.getAnnotatedElement(OutgoingQueue.class)) {
            OutgoingQueue annotation = annotatedElement.getAnnotation(OutgoingQueue.class);
            queueConnectionMap.put(annotation.value(), annotation);
        }

        Config queueConfig = configService.get("queue");

        Map<String, MicroProfileQueueConfig> newMap = new HashMap<>();
        for (QueueProducer queueProducer: queueProducerInstances) {
            if (newMap.containsKey(queueProducer.getQueueName())) {
                throw new ConfigurationException(QueueService.EX_DUPLICATED_QUEUE,
                        queueProducer.getClass().getName(), newMap.get(queueProducer.getClass().getName()));
            }
            OutgoingQueue outgoingConnection = queueConnectionMap.get(queueProducer.getQueueName());

            newMap.put(queueProducer.getQueueName(), new MicroProfileQueueConfig(queueConfig, outgoingConnection, queueProducer));
            LOGGER.debug("Registered Queue [{}], Producer [{}]", outgoingConnection.value(), queueProducer.getClass().getSimpleName());
        }
        LOGGER.info("Registered [{}] Queues {}", newMap.size(), newMap.keySet());
        queueProducerMap = newMap;
    }

    protected void onStartUp(@Observes ApplicationPreReadyEvent preReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent received");
    }

    @Override
    public Set<String> getQueueNames() {
        return queueProducerMap.keySet();
    }

    @Override
    public void addToQueue(String queueName, Serializable payload) {
        addToQueue(queueName, new QueueMessage(requestDetailsProvider.get(), "", payload));
    }

    @Override
    public void addToQueue(String queueName, QueueMessage message) {
        queueProducerMap.computeIfAbsent(queueName, s -> {
            throw new ConfigurationException(QueueService.EX_NON_EXISTENT_QUEUE, QueueProducer.class.getSimpleName(), queueName); })
                .getQueueProducer().addToQueue(JsonUtil.toJson(message));
    }

    @Override
    public QueueConfig getQueueConfig(String queueName) {
        return queueProducerMap.get(queueName);
    }
}
