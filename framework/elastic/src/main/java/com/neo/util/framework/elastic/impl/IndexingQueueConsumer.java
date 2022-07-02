package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.persistence.search.SearchRepository;
import com.neo.util.framework.api.persistence.search.QueueableSearchable;
import com.neo.util.framework.api.queue.IncomingQueueConnection;
import com.neo.util.framework.api.queue.QueueListener;
import com.neo.util.framework.api.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@IncomingQueueConnection(IndexingQueueService.QUEUE_NAME)
public class IndexingQueueConsumer implements QueueListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexingQueueConsumer.class);

    @Inject
    protected SearchRepository searchRepository;

    @Override
    public void onMessage(QueueMessage message) {
        String messageType = message.getMessageType();
        if (searchRepository.enabled()) {
            if (QueueableSearchable.RequestType.BULK.toString().equals(messageType)) {
                List<QueueableSearchable> transportSearchables = (List<QueueableSearchable>) message.getMessage();
                searchRepository.process(transportSearchables);
            } else {
                QueueableSearchable transportSearchable = (QueueableSearchable) message.getMessage();
                searchRepository.process(transportSearchable);
            }
        }
    }
}
