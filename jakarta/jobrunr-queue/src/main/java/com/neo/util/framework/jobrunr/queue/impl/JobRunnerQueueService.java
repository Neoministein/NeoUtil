package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.queue.*;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.security.InstanceIdentification;
import com.neo.util.framework.impl.ReflectionService;
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
import java.lang.reflect.AnnotatedElement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Priority(PriorityConstants.APPLICATION)
@Alternative
@ApplicationScoped
public class JobRunnerQueueService implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerQueueService.class);

    public static final ExceptionDetails EX_MISSING_OUTGOING = new ExceptionDetails(
            "queue/missing-outgoing-annotation", "The JobRunner implementation also requires an outgoing annotation for [{0}]", true);

    @Inject
    protected ConfigService configService;

    @Inject
    protected RequestContextExecutor requestContextExecutor;

    @Inject
    protected InstanceIdentification instanceIdentification;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    protected Map<String, JobRunnerQueueConfig> queueListenerMap = new HashMap<>();

    public void readyEvent(@Observes ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.debug("ApplicationReadyEvent processed");
    }

    @Inject
    public void init(Instance<QueueListener> queueListeners, ReflectionService reflectionService) {
        Map<String, OutgoingQueue> queueConnectionMap = new HashMap<>();
        for (AnnotatedElement annotatedElement: reflectionService.getAnnotatedElement(OutgoingQueue.class)) {
            OutgoingQueue annotation = annotatedElement.getAnnotation(OutgoingQueue.class);
            queueConnectionMap.put(annotation.value(), annotation);
        }

        Map<String, JobRunnerQueueConfig> newMap = new HashMap<>();
        Config queueConfig = configService.get("queue");

        for (QueueListener queueListener: queueListeners) {
            Class<?> clazz = queueListener.getClass().getSuperclass();

            IncomingQueue incomingAnnotation = clazz.getAnnotation(IncomingQueue.class);

            if (newMap.containsKey(incomingAnnotation.value())) {
                throw new ConfigurationException(QueueService.EX_DUPLICATED_QUEUE, queueListener.getClass().getName(), newMap.get(incomingAnnotation.value()));
            }

            OutgoingQueue outgoingConnection = queueConnectionMap.get(incomingAnnotation.value());
            if (outgoingConnection == null) {
                throw new ConfigurationException(EX_MISSING_OUTGOING, incomingAnnotation.value());
            }

            LOGGER.debug("Registered Queue [{}], Listener [{}]", incomingAnnotation.value(), queueListener.getClass().getSimpleName());
            newMap.put(incomingAnnotation.value(), new JobRunnerQueueConfig(queueConfig, outgoingConnection, queueListener));
        }
        LOGGER.info("Registered [{}] Queues {}", newMap.size(), newMap.keySet());
        queueListenerMap = newMap;
    }

    @Override
    public Set<String> getQueueNames() {
        return queueListenerMap.keySet();
    }

    @Override
    public void addToQueue(String queueName, Serializable payload) {
        addToQueue(queueName, new QueueMessage(requestDetailsProvider.get(), "", payload));
    }

    @Override
    public void addToQueue(String queueName, QueueMessage message) {
        JobRunnerQueueConfig config = queueListenerMap.computeIfAbsent(queueName, s -> {
                    throw new ConfigurationException(QueueService.EX_NON_EXISTENT_QUEUE, QueueListener.class.getSimpleName(), queueName); });

        BackgroundJob.create(createJob(config, message));
    }

    @Override
    public QueueConfig getQueueConfig(String queueName) {
        return queueListenerMap.get(queueName);
    }

    public void queueAction(String queueName, QueueMessage message) {
        JobRunnerQueueConfig config = queueListenerMap.get(queueName);

        try {
            requestContextExecutor.execute(new QueueRequestDetails(instanceIdentification.getInstanceId(), message, config.getRequestContext()), () -> config.getQueueListener().onMessage(message));
        } catch (RuntimeException ex) {
            LOGGER.error("Unexpected error occurred while processing a queue [{}], action won't be retried.", ex.getMessage());
            throw ex;
        }

    }

    protected JobBuilder createJob(JobRunnerQueueConfig config, QueueMessage message) {
        JobBuilder jobBuilder = JobBuilder.aJob();
        jobBuilder.withDetails(() -> queueAction(config.getQueueName(), message));
        jobBuilder.scheduleAt(Instant.now().plus(config.getDuration()));
        jobBuilder.withAmountOfRetries(config.getRetry());
        return jobBuilder;
    }
}
