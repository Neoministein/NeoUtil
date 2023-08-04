package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.JobBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Priority(PriorityConstants.APPLICATION)
@Alternative
@ApplicationScoped
public class JobRunnerQueueService implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerQueueService.class);

    @Inject
    protected ConfigService configService;

    @Inject
    protected RequestContextExecutor requestContextExecutor;

    @Inject
    protected InstanceIdentification instanceIdentification;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    protected Map<String, QueueListenerConfig> queueListenerMap = new HashMap<>();

    public void readyEvent(@Observes ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.debug("ApplicationReadyEvent processed");
    }

    @Inject
    public void init(Instance<QueueListener> queueListeners) {
        Map<String, QueueListenerConfig> newMap = new HashMap<>();
        for (QueueListener queueListener: queueListeners) {
            Class<?> clazz = queueListener.getClass().getSuperclass();

            IncomingQueueConnection queueAnnotation = clazz.getAnnotation(IncomingQueueConnection.class);
            if (newMap.containsKey(queueAnnotation.value())) {
                throw new ConfigurationException(QueueService.EX_DUPLICATED_QUEUE, queueListener.getClass().getName(), newMap.get(queueAnnotation.value()));
            }

            Config queueConfig = configService.get("queue").get(queueAnnotation.value());
            int retry = queueConfig.get("retry").asInt().orElse(0);
            int delay = queueConfig.get("delay").asInt().orElse(0);
            TimeUnit timeUnit = queueConfig.get("time-unit").asString().map(TimeUnit::valueOf).orElse(TimeUnit.SECONDS);

            LOGGER.debug("Registered Queue [{}], Listener [{}]", queueAnnotation.value(), queueListener.getClass().getSimpleName());
            newMap.put(queueAnnotation.value(), new QueueListenerConfig(queueAnnotation.value(), retry, delay, timeUnit, queueListener));
        }
        LOGGER.info("Registered [{}] Queues {}", newMap.size(), newMap.keySet());
        queueListenerMap = newMap;
    }

    @Override
    public void addToQueue(String queueName, Serializable payload) {
        addToQueue(queueName, new QueueMessage(requestDetailsProvider.get(), "", payload));
    }

    @Override
    public void addToQueue(String queueName, QueueMessage message) {
        QueueListenerConfig config = queueListenerMap.computeIfAbsent(queueName, s -> {
                    throw new ConfigurationException(QueueService.EX_NON_EXISTENT_QUEUE, QueueListener.class.getSimpleName(), queueName); });

        BackgroundJob.create(createJob(config, message));
    }

    public void queueAction(String queueName, QueueMessage message) {
        QueueListenerConfig config = queueListenerMap.get(queueName);

        try {
            requestContextExecutor.execute(new QueueRequestDetails(instanceIdentification.getInstanceId(), message, config.getRequestContext()), () -> config.getQueueListener().onMessage(message));
        } catch (RuntimeException ex) {
            LOGGER.error("Unexpected error occurred while processing a queue [{}], action won't be retried.", ex.getMessage());
            throw ex;
        }

    }

    protected JobBuilder createJob(QueueListenerConfig config, QueueMessage message) {
        JobBuilder jobBuilder = JobBuilder.aJob();
        jobBuilder.withDetails(() -> queueAction(config.getQueueName(), message));
        jobBuilder.scheduleAt(Instant.now().plus(config.getDuration()));
        jobBuilder.withAmountOfRetries(config.getRetry());
        return jobBuilder;
    }
}
