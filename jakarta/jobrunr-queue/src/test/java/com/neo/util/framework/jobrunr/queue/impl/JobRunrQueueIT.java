package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.common.impl.MathUtils;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.jobrunr.impl.JobRunnerConfigurator;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jobrunr.configuration.JobRunr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ExtendWith(WeldJunit5Extension.class)
class JobRunrQueueIT {

    @WeldSetup
    protected WeldInitiator weld = WeldInitiator.from(new Weld()).activate(RequestScoped.class).build();

    protected JobRunnerQueueService queueService;

    protected BasicQueueListener basicQueueListener;
    protected DelayedQueueListener delayedQueueListener;
    protected RetryQueueListener retryQueueListener;

    @BeforeEach
    void init() {
        weld.select(ConfigService.class).get().save(0.2f, JobRunnerConfigurator.CONFIG_PREFIX + JobRunnerConfigurator.CONFIG_POLL_INTERVAL);
        weld.select(ConfigService.class).get().save(5, "queue." + DelayedQueueListener.QUEUE_NAME + ".delay");
        weld.select(ConfigService.class).get().save(5, "queue." + RetryQueueListener.QUEUE_NAME + ".retry");

        weld.select(JobRunnerConfigurator.class).get().preReadyEvent(new ApplicationPreReadyEvent());

        queueService = weld.select(JobRunnerQueueService.class).get();
        queueService.readyEvent(new ApplicationReadyEvent());

        basicQueueListener = weld.select(BasicQueueListener.class).get();
        delayedQueueListener = weld.select(DelayedQueueListener.class).get();
        retryQueueListener = weld.select(RetryQueueListener.class).get();
    }

    @AfterEach
    public void stopJobRunr() {
        JobRunr.destroy();
    }

    @Test
    void jobRunrTest() {
        Instant start = Instant.now();

        QueueMessage basicMessage = new QueueMessage(create(BasicQueueListener.QUEUE_NAME), "messageType", "basicPayload");

        queueService.addToQueue(BasicQueueListener.QUEUE_NAME, basicMessage);

        IntegrationTestUtil.sleepUntil(200, 30, () -> {
            Assertions.assertNotNull(basicQueueListener.getLastMessage());
        });

        int basicDelay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(basicDelay,0, 2000), "The delay " + basicDelay);


        //DelayTest

        start = Instant.now();

        QueueMessage delayMessage = new QueueMessage(create(DelayedQueueListener.QUEUE_NAME), "messageType", "delayedPayload");

        queueService.addToQueue(DelayedQueueListener.QUEUE_NAME, delayMessage);

        Assertions.assertNull(delayedQueueListener.getLastMessage());

        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertNotNull(delayedQueueListener.getLastMessage());
        });

        int delay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(delay,3000, 7000), "The delay " + delay);
        //RetryTest

        QueueMessage retryMessage = new QueueMessage(create(RetryQueueListener.QUEUE_NAME), "messageType", "retryPayload");

        queueService.addToQueue(RetryQueueListener.QUEUE_NAME, retryMessage);

        IntegrationTestUtil.sleepUntil(500, 30, () -> {
            Assertions.assertFalse(retryQueueListener.isFirst());
            Assertions.assertNotNull(retryQueueListener.getLastMessage());
        });
    }

    private RequestDetails create(String queueName) {
        return new QueueRequestDetails("", "", "", new QueueRequestDetails.Context(queueName));
    }
}
