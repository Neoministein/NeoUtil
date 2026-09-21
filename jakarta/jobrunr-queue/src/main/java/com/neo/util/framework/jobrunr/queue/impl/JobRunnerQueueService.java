package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.api.config.ConfigService;
import com.neo.util.api.event.ApplicationReadyEvent;
import com.neo.util.api.queue.*;
import com.neo.util.api.request.RequestDetails;
import com.neo.util.common.api.PriorityConstants;
import com.neo.util.common.api.reflection.ReflectionProvider;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.NoContentFoundException;
import com.neo.util.jakarta.request.InstanceIdentification;
import com.neo.util.jakarta.request.RequestContextExecutor;
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
            "queue/missing-outgoing-annotation", "The JobRunner implementation also requires an outgoing annotation for [{0}]");

    protected final ConfigService configService;
    protected final InstanceIdentification instanceIdentification;
    protected final Provider<RequestDetails> requestDetailsProvider;
    protected final RequestContextExecutor requestContextExecutor;

    protected final Map<String, JobRunnerQueueConfig> queueListenerMap = new HashMap<>();

    @Inject
    public JobRunnerQueueService(ConfigService configService, RequestContextExecutor requestContextExecutor, InstanceIdentification instanceIdentification, Provider<RequestDetails> requestDetailsProvider, Instance<QueueListener> queueListeners, ReflectionProvider reflectionProvider) {
        this.configService = configService;
        this.requestContextExecutor = requestContextExecutor;
        this.instanceIdentification = instanceIdentification;
        this.requestDetailsProvider = requestDetailsProvider;

        Map<String, OutgoingQueue> queueConnectionMap = new HashMap<>();
        for (AnnotatedElement annotatedElement: reflectionProvider.getAnnotatedElement(OutgoingQueue.class)) {
            OutgoingQueue annotation = annotatedElement.getAnnotation(OutgoingQueue.class);
            queueConnectionMap.put(annotation.value(), annotation);
        }

        for (QueueListener queueListener: queueListeners) {
            Class<?> clazz = queueListener.getClass().getSuperclass();

            IncomingQueue incomingAnnotation = clazz.getAnnotation(IncomingQueue.class);
            String queueName = incomingAnnotation.value();
            if (queueListenerMap.containsKey(queueName)) {
                throw new ConfigurationException(QueueService.EX_DUPLICATED_QUEUE, queueListener.getClass().getName(), queueListenerMap.get(queueName).toString());
            }

            OutgoingQueue outgoingConnection = queueConnectionMap.get(queueName);
            if (outgoingConnection == null) {
                throw new ConfigurationException(EX_MISSING_OUTGOING, queueName);
            }

            LOGGER.debug("Registered Queue [{}], Listener [{}]", incomingAnnotation.value(), queueListener.getClass().getSimpleName());
            QueueConfig queueConfig = new QueueConfig(configService, outgoingConnection);
            queueListenerMap.put(incomingAnnotation.value(), new JobRunnerQueueConfig(queueConfig, queueListener));
        }
        LOGGER.info("Registered [{}] Queues {}", queueListenerMap.size(), queueListenerMap.keySet());
    }

    public void readyEvent(@Observes ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.debug("ApplicationReadyEvent processed");
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
        BackgroundJob.create(createJob(requestQueueConfig(queueName), message));
    }

    @Override
    public QueueConfig requestQueueConfig(String queueName) {
        JobRunnerQueueConfig queueConfig = queueListenerMap.get(queueName);
        if (queueConfig == null) {
            throw new NoContentFoundException(QueueService.EX_UNKNOWN_QUEUE, queueName);
        }
        return queueConfig.getConfig();
    }

    public void queueAction(String queueName, QueueMessage message) {
        JobRunnerQueueConfig config = queueListenerMap.get(queueName);

        try {
            requestContextExecutor.execute(new QueueRequestDetails(instanceIdentification.getInstanceId(), message, config.getRequestContext()), () -> config.getQueueListener().onMessage(message));
        } catch (RuntimeException ex) {
            LOGGER.error("Unexpected error occurred while processing a queue message. Action will be retried based on the retry policy.", ex);
            throw ex;
        }
    }

    protected JobBuilder createJob(QueueConfig config, QueueMessage message) {
        JobBuilder jobBuilder = JobBuilder.aJob();
        jobBuilder.withJobLambda(() -> queueAction(config.getQueueName(), message));
        jobBuilder.scheduleAt(Instant.now().plus(config.getDuration()));
        jobBuilder.withAmountOfRetries(config.getRetry());
        return jobBuilder;
    }
}
