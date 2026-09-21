package com.neo.util.jakarta.elastic;

import com.neo.util.api.persistence.search.QueueableSearchable;
import com.neo.util.api.persistence.search.SearchProvider;
import com.neo.util.api.queue.IncomingQueue;
import com.neo.util.api.queue.QueueListener;
import com.neo.util.api.queue.QueueMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
@IncomingQueue(IndexingQueueService.QUEUE_NAME)
public class IndexingQueueConsumer implements QueueListener {

    protected SearchProvider searchRepository;

    @Inject
    public IndexingQueueConsumer(SearchProvider searchRepository) {
        this.searchRepository = searchRepository;
    }

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
