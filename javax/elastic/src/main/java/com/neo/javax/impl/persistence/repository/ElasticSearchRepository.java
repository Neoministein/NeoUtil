package com.neo.javax.impl.persistence.repository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.common.impl.enumeration.Synchronization;
import com.neo.common.impl.exception.InternalLogicException;
import com.neo.javax.api.config.ConfigService;
import com.neo.javax.api.event.ElasticSearchConnectionStatusEvent;
import com.neo.javax.api.persistence.repository.IndexNamingService;
import com.neo.javax.api.persitence.IndexParameter;
import com.neo.javax.api.persitence.SearchParameters;
import com.neo.javax.api.persitence.SearchResult;
import com.neo.javax.api.persitence.entity.Searchable;
import com.neo.javax.api.persitence.repository.SearchRepository;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class provides methods to interact with elastic search
 */
@ApplicationScoped
public class ElasticSearchRepository implements SearchRepository {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepository.class);

    //{"I/O reactor status: STOPPED","I/O reactor has been shut down"}
    protected static final String[] FILTER_HTTPCLIENT_MESSAGES = new String[] { "I/O reactor" };

    protected static final int RETRY_ON_CONFLICT = 3;

    @Inject
    ConfigService configService;

    @Inject IndexNamingService indexNameService;

    @Inject
    ElasticSearchConnectionRepository connection;

    private volatile BulkProcessor bulkProcessor;

    //TODO MAKE THIS BETTER
    protected synchronized void setupBulkProcessor() {
        if (bulkProcessor != null) {
            return;
        }

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest bulkRequest) {
                int numberOfActions = bulkRequest.numberOfActions();
                LOGGER.debug("Executing bulk [{}] with {} requests, first doc [{}]", executionId, numberOfActions,
                            numberOfActions > 0 ? bulkRequest.requests().get(0) : "Empty");

            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                LOGGER.info("Executed bulk [{}] with {} requests, hasFailures: {}, took: {}, ingestTook: {}",
                        executionId, bulkRequest.numberOfActions(), bulkResponse.hasFailures(), bulkResponse.getTook(),
                        bulkResponse.getIngestTook());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, Throwable failure) {
                LOGGER.info("Executed bulk [{}] with failure complete bulk will be retried, message: {}", executionId,
                        failure.getMessage());
            }
        };

        try {
            BulkProcessor.Builder builder = createBulkRequestBuilder(listener);
            bulkProcessor = builder.build();
        } catch (IllegalStateException e) {
            LOGGER.error("Unable to create bulk processor", e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        closeBulkProcessor();
        disconnect();
    }

    protected void connectionStatusListener(@Observes ElasticSearchConnectionStatusEvent event) {
        if (ElasticSearchConnectionStatusEvent.STATUS_EVENT_CONNECTED.equals(event.getConnectionStatus())) {
            setupBulkProcessor();
        }
    }

    protected BulkProcessor getBulkProcessor() {
        if (bulkProcessor == null) {
            getClient();
            throw new IllegalStateException("Elasticsearch bulkProcessor not ready");
        }
        return bulkProcessor;
    }

    protected RestHighLevelClient getClient() {
        return connection.getClient();
    }

    public void index(Searchable searchable) {
        index(searchable, new IndexParameter());
    }

    public void index(List<? extends Searchable> searchableList) {
        index(searchableList, new IndexParameter());
    }

    public void index(Searchable searchable, IndexParameter indexParameter) {
        final IndexRequest indexRequest = generateIndexRequest(searchable);
        if (Synchronization.ASYNCHRONOUS == indexParameter.getSynchronization()) {
            addToBulkProcessor(indexRequest);
        } else {
            try {
                getClient().index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException ex) {
                throw new InternalLogicException("IOException while synchronous indexing", ex);
            } catch (IllegalStateException ex) {
                reconnectClientIfNeeded(ex);
                throw ex;
            }
        }
    }

    public void index(List<? extends Searchable> searchableList, IndexParameter indexParameter) {
        if (Synchronization.ASYNCHRONOUS.equals(indexParameter.getSynchronization())) {
            for (Searchable searchable : searchableList) {
                getBulkProcessor().add(generateIndexRequest(searchable));
            }
        } else {
            final BulkRequest bulkRequest = new BulkRequest();

            for (Searchable searchable : searchableList) {
                bulkRequest.add(generateIndexRequest(searchable));
            }

            try {
                getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
            } catch (IOException ex) {
                throw new InternalLogicException("IOException while synchronous indexing bulk", ex);
            } catch (IllegalStateException ex) {
                reconnectClientIfNeeded(ex);
                throw ex;
            }
        }
    }

    //TODO IMPLEMENT THIS
    @Override
    public void update(Searchable searchable, boolean partial) {
        update(searchable, partial, new IndexParameter());
    }

    @Override
    public void update(Searchable searchable, boolean partial, IndexParameter indexParameter) {
        throw new InternalLogicException("Not implemented yet");
    }

    @Override
    public void update(List<? extends Searchable> searchableList, boolean partial) {
        throw new InternalLogicException("Not implemented yet");
    }

    @Override
    public void delete(Searchable searchable) {
        throw new InternalLogicException("Not implemented yet");
    }

    @Override
    public void delete(List<? extends Searchable> searchableList) {
        throw new InternalLogicException("Not implemented yet");
    }

    @Override
    public void deleteAll(Class<? extends Searchable> searchableClazz) {
        throw new InternalLogicException("Not implemented yet");
    }

    @Override
    public SearchResult fetch(String index, SearchParameters parameters) {
        throw new InternalLogicException("Not implemented yet");
    }

    protected void addToBulkProcessor(DocWriteRequest<?> request) {
        try {
            getBulkProcessor().add(request);
        } catch (IllegalStateException ex) {
            LOGGER.info("Error while adding to BulkProcessor, message: {}", ex.getMessage());
            //TODO add to retry que
        }
    }

    protected IndexRequest generateIndexRequest(Searchable searchable) {
        String indexName = indexNameService.getIndexName(searchable);
        return new IndexRequest(indexName)
                .source(parseSearchableToObjectNode(searchable).toString(), XContentType.JSON)
                .id(searchable.getBusinessId());
    }

    protected ObjectNode parseSearchableToObjectNode(Searchable searchable) {
        if (searchable.getBusinessId() == null) {
            throw new InternalLogicException("Error while parsing searchable to json: BusinessId key cannot be null");
        }
        try {
            ObjectNode objectNode = searchable.getJsonNode();
            objectNode.put(Searchable.BUSINESS_ID, searchable.getBusinessId());
            objectNode.put(Searchable.TYPE, searchable.getClassName());
            return objectNode;
        } catch (IllegalArgumentException e) {
            throw new InternalLogicException("Error while parsing searchable to json: " + searchable.getClassName() + ":" + searchable.getBusinessId(), e);
        }
    }

    public void reconnect() {
        closeBulkProcessor();
        connection.disconnect();
        connection.connect();
    }

    protected void disconnect() {
        connection.disconnect();
    }

    /**
     * In some situations the underlying http client used by elasticsearch's high level rest client stops proceeding and
     * returns IllegalStateException.
     *
     * @param ex the exception to check
     * @return true if the clients has reconnected
     */
    protected boolean reconnectClientIfNeeded(IllegalStateException ex) {
        if (Arrays.stream(FILTER_HTTPCLIENT_MESSAGES).anyMatch(ex.getMessage()::contains)) {
            LOGGER.info("IllegalStateException reconnectClient");
            reconnect();
            return true;
        }
        return false;
    }

    /**
     * prepare shutdown and close bulk processor
     */
    protected void closeBulkProcessor() {
        // make sure all bulk requests have been processed
        try {
            if (bulkProcessor != null) {
                bulkProcessor.awaitClose(30L,
                        TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOGGER.warn("exception while closing bulkProcessor, error: {}", e.getMessage());
        }
        bulkProcessor = null;
    }

    protected BulkProcessor.Builder createBulkRequestBuilder(BulkProcessor.Listener listener) {
        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) ->
                        getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener);

        configService.get(ElasticSearchConnectionRepository.ELASTIC_CONFIG);

        int flushInterval = configService.get("FlushInterval").asInt().orElse(10);
        LOGGER.info("BulkProcessor.Builder FlushInterval: {}", flushInterval);
        builder.setFlushInterval(TimeValue.timeValueSeconds(flushInterval));

        int bulkAction = configService.get("BulkActions").asInt().orElse(2500);
        LOGGER.info("BulkProcessor.Builder BulkActions: {}", bulkAction);
        builder.setBulkActions(bulkAction);

        int concurrentRequests = configService.get("ConcurrentRequests").asInt().orElse(3);
        LOGGER.info("BulkProcessor.Builder ConcurrentRequests: {}", concurrentRequests);
        builder.setConcurrentRequests(concurrentRequests);

        int bulkSize = configService.get("BulkSize").asInt().orElse(10);
        LOGGER.info("BulkProcessor.Builder BulkSize: {}", bulkSize);
        builder.setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB));

        return builder;
    }
}
