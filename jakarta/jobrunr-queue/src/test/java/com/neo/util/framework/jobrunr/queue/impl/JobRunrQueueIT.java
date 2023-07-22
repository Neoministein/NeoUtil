package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.common.impl.MathUtils;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.request.RequestContext;
import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.api.queue.QueueService;
import com.neo.util.framework.impl.config.BasicConfigService;
import com.neo.util.framework.impl.config.BasicConfigValue;
import com.neo.util.framework.impl.request.QueueRequestDetails;
import com.neo.util.framework.jobrunr.queue.impl.config.JobRunnerConfigurator;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jobrunr.configuration.JobRunr;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ExtendWith(WeldJunit5Extension.class)
class JobRunrQueueIT {

    private static final BasicConfigValue<Integer> POOL_INTERVAL = new BasicConfigValue<>(JobRunnerConfigurator.CONFIG_PREFIX + JobRunnerConfigurator.CONFIG_POLL_INTERVAL, 1);

    private static final BasicConfigValue<Integer> DELAY_5_SEC = new BasicConfigValue<>(JobRunnerConfigurator.CONFIG_PREFIX + JobRunnerQueueService.CONFIG_QUEUE + DelayedQueueListener.QUEUE_NAME, 5);

    @WeldSetup
    protected WeldInitiator weld = WeldInitiator.from(new Weld()).activate(RequestScoped.class).build();

    protected QueueService queueService;

    protected BasicQueueListener basicQueueListener;
    protected DelayedQueueListener delayedQueueListener;

    @BeforeEach
    void init() {
        weld.select(BasicConfigService.class).get().save(POOL_INTERVAL);
        weld.select(BasicConfigService.class).get().save(DELAY_5_SEC);

        weld.select(JobRunnerConfigurator.class).get().preReadyEvent(new ApplicationPreReadyEvent());

        queueService = weld.select(JobRunnerQueueService.class).get();
        basicQueueListener = weld.select(BasicQueueListener.class).get();
        delayedQueueListener = weld.select(DelayedQueueListener.class).get();
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
            return true;
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
            return true;
        });

        int delay = (int) Instant.now().minus(start.toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli();
        Assertions.assertTrue(MathUtils.isInBounds(delay,4000, 6000), "The delay " + delay);
    }

    private RequestDetails create(String queueName) {
        return new QueueRequestDetails(queueName, UUID.randomUUID().toString(), new QueueRequestDetails.Context(queueName));
    }
}
