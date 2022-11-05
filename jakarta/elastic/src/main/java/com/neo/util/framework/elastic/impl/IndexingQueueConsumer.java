package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.persistence.search.SearchProvider;
import com.neo.util.framework.api.persistence.search.QueueableSearchable;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
@IncomingQueueConnection(IndexingQueueService.QUEUE_NAME)
public class IndexingQueueConsumer implements QueueListener {

    @Inject
    protected SearchProvider searchRepository;

    @Override
    public void onMessage(QueueMessage message) {
        String messageType = message.getMessageType();
        if (searchRepository.enabled()) {
            if (QueueableSearchable.RequestType.BULK.toString().equals(messageType)) {
                List<QueueableSearchable> transportSearchableList = (List<QueueableSearchable>) message.getPayload();
                searchRepository.process(transportSearchableList);
            } else {
                QueueableSearchable transportSearchable = (QueueableSearchable) message.getPayload();
                searchRepository.process(transportSearchable);
            }
        }
    }
}
