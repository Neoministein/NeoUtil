package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.request.RequestContextExecutor;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.jobrunr.queue.impl.config.JobRunnerConfigurator;
import com.neo.util.framework.jobrunr.queue.impl.config.QueueListenerConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Priority(PriorityConstants.APPLICATION)
@Alternative
@ApplicationScoped
public class JobRunnerQueueService implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerQueueService.class);

    public static final String CONFIG_QUEUE = "queue.";

    @Inject
    protected ConfigService configService;

    @Inject
    protected RequestContextExecutor requestContextExecutor;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    protected Map<String, QueueListenerConfig> queueListenerMap = new HashMap<>();

    protected JobRunrCDIQueueEntryPoint jobRunrCDIQueueEntryPoint;

    public JobRunnerQueueService() {
        this.jobRunrCDIQueueEntryPoint = new JobRunrCDIQueueEntryPoint();
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

            LOGGER.debug("Registered Queue [{}], Listener [{}]", queueAnnotation.value(), queueListener.getClass().getSimpleName());
            newMap.put(queueAnnotation.value(), new QueueListenerConfig(queueAnnotation.value(), configService.get(
                    JobRunnerConfigurator.CONFIG_PREFIX + CONFIG_QUEUE + queueAnnotation.value()).asInt().orElse(0), queueListener));
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

        JobLambda action = () -> config.getQueueListener().onMessage(message);

        if (config.hasDelay()) {
            BackgroundJob.schedule(Instant.now().plus(Duration.ofSeconds(config.getDelayInSeconds())), action);
        } else {
            BackgroundJob.enqueue((action));
        }
    }

    public void readyEvent(@Observes ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.debug("ApplicationReadyEvent processed");
    }

    public void queueAction(String queueName, QueueMessage message) {
        QueueListenerConfig config = queueListenerMap.get(queueName);

        try {
            requestContextExecutor.execute(new QueueRequestDetails(message, config.getRequestContext()), () -> config.getQueueListener().onMessage(message));
        } catch (RuntimeException ex) {
            LOGGER.error("Unexpected error occurred while processing a queue [{}], action won't be retried.", ex.getMessage());
            throw ex;
        }

    }
}
