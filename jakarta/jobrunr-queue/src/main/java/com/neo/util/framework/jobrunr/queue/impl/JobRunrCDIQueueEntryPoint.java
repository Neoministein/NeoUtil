package com.neo.util.framework.jobrunr.queue.impl;

import com.neo.util.framework.api.queue.QueueMessage;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Set;

public class JobRunrCDIQueueEntryPoint {

    protected static JobRunnerQueueService queueServiceCdiProxy;

    static {
        BeanManager beanManager = CDI.current().getBeanManager();

        Set<Bean<?>> beans = beanManager.getBeans(JobRunnerQueueService.class);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> context = beanManager.createCreationalContext(bean);
        Object proxy = beanManager.getReference(bean, JobRunnerQueueService.class, context);
        queueServiceCdiProxy = (JobRunnerQueueService) proxy;
    }

    public void submit(String queueName, QueueMessage message) {
        queueServiceCdiProxy.queueAction(queueName, message);
    }

}
