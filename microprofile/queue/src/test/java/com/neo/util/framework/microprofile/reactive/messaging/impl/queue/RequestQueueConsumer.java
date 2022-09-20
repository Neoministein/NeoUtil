package com.neo.util.framework.microprofile.reactive.messaging.impl.queue;

import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.microprofile.reactive.messaging.impl.BasicRequestScopedBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@IncomingQueueConnection(RequestQueueService.QUEUE_NAME)
public class RequestQueueConsumer implements QueueListener {

    protected List<QueueMessage> messages = new ArrayList<>();

    @Inject
    BasicRequestScopedBean basicRequestScopedBean;

    @Override
    public void onMessage(QueueMessage message) {
        basicRequestScopedBean.triggerClassMethod();
        messages.add(message);
    }

    public List<QueueMessage> getMessages() {
        return messages;
    }
}
