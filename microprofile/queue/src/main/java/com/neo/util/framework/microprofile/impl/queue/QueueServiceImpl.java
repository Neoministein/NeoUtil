package com.neo.util.framework.microprofile.impl.queue;

import com.neo.util.common.impl.exception.InternalConfigurationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueProducer;
import com.neo.util.framework.api.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Priority(PriorityConstants.APPLICATION)
@Alternative
@ApplicationScoped
public class QueueServiceImpl implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    protected Map<String, QueueProducer> queueProducerMap = new HashMap<>();

    /**
     * Initializes the mapping to the {@link QueueProducer}.
     * This is done only once at startup as no new queues should be added at runtime.
     */
    @Inject
    protected void init(Instance<QueueProducer> queueProducerInstances) {
        for (QueueProducer queueProducer: queueProducerInstances) {
            if (queueProducerMap.containsKey(queueProducer.getQueueName())) {
                LOGGER.error("Found duplicated queue {} {} {}",
                        queueProducer.getQueueName(),
                        queueProducer.getClass().getSimpleName(),
                        queueProducerMap.get(queueProducer.getQueueName()));
                throw new InternalConfigurationException("Duplicated queue present");
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
    public void addToQueue(String queueName, QueueMessage message) {
        queueProducerMap.computeIfAbsent(queueName, s -> { throw new InternalConfigurationException(); })
                .addToQueue(JsonUtil.toJson(message));
    }
}
