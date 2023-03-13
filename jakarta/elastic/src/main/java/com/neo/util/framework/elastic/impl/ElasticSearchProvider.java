package com.neo.util.framework.elastic.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.*;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.enumeration.Association;
import com.neo.util.common.impl.enumeration.Synchronization;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.api.persistence.aggregation.*;
import com.neo.util.framework.api.persistence.criteria.*;
import com.neo.util.framework.api.persistence.search.*;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionProvider;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionStatusEvent;
import com.neo.util.framework.elastic.api.IndexNamingService;
import com.neo.util.framework.elastic.api.aggregation.BucketScriptAggregation;
import jakarta.inject.Provider;
import jakarta.json.spi.JsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class provides methods to interact with elastic search
 */
@Alternative
@Priority(PriorityConstants.APPLICATION)
@ApplicationScoped
public class ElasticSearchProvider implements SearchProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchProvider.class);

    protected static final long BYTES_IN_MB = 1000000;

    protected static final String[] FILTER_HTTPCLIENT_MESSAGES = new String[] { "I/O reactor" };

    protected static final String CONFIG_PREFIX = "elastic";

    public static final String FLUSH_INTERVAL_CONFIG = CONFIG_PREFIX + ".FlushInterval";
    public static final String BULK_ACTION_CONFIG = CONFIG_PREFIX + ".BulkActions";
    public static final String CONCURRENT_REQUEST_CONFIG = CONFIG_PREFIX + ".ConcurrentRequests";
    public static final String BULK_SIZE = CONFIG_PREFIX + ".BulkSize";

    protected static final ExceptionDetails EX_SYNCHRONOUS_INDEXING = new ExceptionDetails(
            "elk/synchronous-indexing", "IOException while synchronous indexing", true);

    protected static final ExceptionDetails EX_IO_SEARCHING = new ExceptionDetails(
            "elk/io-searching", "Failed to fetch {0} entries for index {1} because of IOException: {2}", true);

    protected static final ExceptionDetails EX_CONFIG_SEARCHING = new ExceptionDetails(
            "elk/io-searching", "Failed to fetch {0} entries for index {1} because of ElasticsearchException: {2}", true);

    @Inject
    protected ConfigService configService;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @Inject
    protected IndexingQueueService indexerQueueService;

    @Inject
    protected IndexNamingService indexNameService;

    @Inject
    protected ElasticSearchConnectionProvider connection;

    protected volatile BulkIngester<Object> bulkIngester;

    protected synchronized void setupBulkIngester() {
        if (bulkIngester != null) {
            return;
        }

        BulkListener<Object> listener = new BulkListener<>() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request, List<Object> objects) {
                LOGGER.debug("Executing bulk [{}] with [{}] requests", executionId, request.operations().size());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<Object> objects, BulkResponse response) {
                handleBulkResponse(executionId, request, response);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<Object> objects, Throwable failure) {
                LOGGER.info("Executed bulk [{}] with complete failure, entire bulk will be retried, message: [{}]", executionId,
                        failure.getMessage());
                ArrayList<QueueableSearchable> bulkQueueableSearchableList = new ArrayList<>();
                for (BulkOperation operation : request.operations()) {
                    bulkQueueableSearchableList.add(buildQueueableSearchable(operation));
                }
                indexerQueueService.addToIndexingQueue(new QueueMessage("BulkIngester",
                        "BulkIngester-" + executionId,
                        QueueableSearchable.RequestType.BULK.toString(), bulkQueueableSearchableList));
                if (failure instanceof IllegalStateException illegalStateException) {

                    //To not encounter a deadlock if a reconnect is needed since this is called by an elastic thread
                    //https://discuss.elastic.co/t/highlevelrestclient-java-client-does-not-shut-down-after-async-requests/286836
                    new Thread(() -> reconnectClientIfNeeded(illegalStateException));
                }
            }
        };

        try {
            BulkIngester.Builder<Object> builder = createBulkRequestBuilder(listener);
            bulkIngester = builder.build();
        } catch (IllegalStateException e) {
            LOGGER.error("Unable to create bulk processor", e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        closeBulkIngester();
        disconnect();
    }

    public void connectionStatusListener(@Observes ApplicationReadyEvent event) {
        setupBulkIngester();
    }

    public void connectionStatusListener(@Observes ElasticSearchConnectionStatusEvent event) {
        if (ElasticSearchConnectionStatusEvent.STATUS_EVENT_CONNECTED.equals(event.getConnectionStatus())) {
            setupBulkIngester();
        }
    }

    protected BulkIngester<Object> getBulkIngester() {
        if (bulkIngester == null) {
            getApiClient();
            throw new IllegalStateException("Elasticsearch bulkProcessor not ready");
        }
        return bulkIngester;
    }

    protected ElasticsearchClient getApiClient() {
        return connection.getApiClient();
    }

    public void index(Searchable searchable) {
        index(searchable, new IndexParameter());
    }

    public void index(Collection<? extends Searchable> searchableList) {
        index(searchableList, new IndexParameter());
    }

    public void index(Searchable searchable, IndexParameter indexParameter) {
        if (Synchronization.ASYNCHRONOUS == indexParameter.getSynchronization()) {
            addToBulkProcessor(buildIndexOperation(searchable));
        } else {
            try {
                getApiClient().index(buildIndexRequest(searchable));
            } catch (IOException ex) {
                throw new CommonRuntimeException(ex, EX_IO_SEARCHING);
            } catch (IllegalStateException ex) {
                reconnectClientIfNeeded(ex);
                throw ex;
            } catch (ElasticsearchException ex) {
                throw ex;
            }
        }
    }

    public void index(Collection<? extends Searchable> searchableList, IndexParameter indexParameter) {
        if (Synchronization.ASYNCHRONOUS.equals(indexParameter.getSynchronization())) {
            for (Searchable searchable : searchableList) {
                getBulkIngester().add(buildIndexOperation(searchable));
            }
        } else {
            final BulkRequest.Builder br = new BulkRequest.Builder();

            for (Searchable searchable : searchableList) {
                br.operations(buildIndexOperation(searchable));
            }
            BulkRequest bulkRequest = br.build();
            try {
                handleBulkResponse(-1, bulkRequest, getApiClient().bulk(bulkRequest));
            } catch (IOException ex) {
                throw new CommonRuntimeException(ex, EX_SYNCHRONOUS_INDEXING, ex);
            } catch (IllegalStateException ex) {
                reconnectClientIfNeeded(ex);
                throw ex;
            }
        }
    }

    @Override
    public void update(Searchable searchable, boolean upsert) {
        update(searchable, upsert, new IndexParameter());
    }

    @Override
    public void update(Searchable searchable, boolean upsert, IndexParameter indexParameter) {
        if (Synchronization.ASYNCHRONOUS == indexParameter.getSynchronization()) {
            addToBulkProcessor(buildUpdateOperation(searchable, upsert));
        } else {
            try {
                getApiClient().update(buildUpdateRequest(searchable, upsert), String.class);
            } catch (IOException ex) {
                throw new CommonRuntimeException(ex, EX_IO_SEARCHING);
            } catch (IllegalStateException ex) {
                reconnectClientIfNeeded(ex);
                throw ex;
            }
        }
    }

    @Override
    public void update(Collection<? extends Searchable> searchableList, boolean upsert) {
        update(searchableList, upsert, new IndexParameter());
    }

    @Override
    public void update(Collection<? extends Searchable> searchableList, boolean upsert, IndexParameter indexParameter) {
        if (Synchronization.ASYNCHRONOUS.equals(indexParameter.getSynchronization())) {
            for (Searchable searchable : searchableList) {
                addToBulkProcessor(buildUpdateOperation(searchable, upsert));
            }
        } else {
            final BulkRequest.Builder br = new BulkRequest.Builder();

            for (Searchable searchable : searchableList) {
                br.operations(buildUpdateOperation(searchable, upsert));
            }

            BulkRequest bulkRequest = br.build();
            try {
                handleBulkResponse(-1, bulkRequest, getApiClient().bulk(bulkRequest));
            } catch (IOException ex) {
                throw new CommonRuntimeException(ex, EX_IO_SEARCHING, ex);
            } catch (IllegalStateException ex) {
                reconnectClientIfNeeded(ex);
                throw ex;
            }
        }
    }

    @Override
    public void delete(Searchable searchable) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void delete(Collection<? extends Searchable> searchableList) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void deleteAll(Class<? extends Searchable> searchableClazz) {
        List<String> indices = getIndicesOfSearchable(searchableClazz);
        if (indices.isEmpty()) {
            LOGGER.info("No indices found for searchable {}", searchableClazz.getSimpleName());
            return;
        }

        DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder().index(indices).build();
        try {
            getApiClient().indices().delete(deleteRequest);
            LOGGER.info("Deleted indices {}", indices);
        } catch (IOException ex) {
            LOGGER.error("Failed to indices for class {}, index names {}, with exception {}",
                    searchableClazz.getSimpleName(), indices, ex.getMessage());
        } catch (ElasticsearchException ex) {
            LOGGER.error("Failed to indices for class {}, index names {}, with exception {}",
                    searchableClazz.getSimpleName(), indices, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void process(QueueableSearchable queueableSearchable) {
        switch (queueableSearchable.getRequestType()) {
        case INDEX -> getBulkIngester().add(buildIndexOperation(queueableSearchable));
        case UPDATE -> getBulkIngester().add(buildUpdateOperation(queueableSearchable));
        case DELETE -> throw new IllegalStateException("Not implemented yet");
        default -> throw new IllegalStateException("Not supported");
        }
    }

    @Override
    public void process(Collection<QueueableSearchable> transportSearchableList) {
        BulkRequest.Builder brBuilder = new BulkRequest.Builder();
        for (QueueableSearchable queueableSearchable : transportSearchableList) {
            switch (queueableSearchable.getRequestType()) {
            case INDEX -> brBuilder.operations(buildIndexOperation(queueableSearchable));
            case UPDATE -> brBuilder.operations(buildUpdateOperation(queueableSearchable));
            case DELETE -> throw new IllegalStateException("Not implemented yet");
            default -> throw new IllegalStateException("Not supported");
            }
        }
        BulkRequest bulkRequest = brBuilder.build();

        try {
            handleBulkResponse(-1,bulkRequest, getApiClient().bulk(bulkRequest));
        } catch (IOException ex) {
            LOGGER.warn("Executed bulk with complete failure, entire bulk will be retried, message: [{}]",
                    ex.getMessage());
            throw new CommonRuntimeException(EX_SYNCHRONOUS_INDEXING);
        } catch (IllegalStateException ex) {
            LOGGER.warn("Executed bulk with complete failure, entire bulk will be retried, message: [{}]",
                    ex.getMessage());
            reconnectClientIfNeeded(ex);
            throw new CommonRuntimeException(EX_SYNCHRONOUS_INDEXING);
        }
    }

    protected void handleBulkResponse(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
        LOGGER.info("Executed bulk [{}] with [{}] requests, hasFailures: [{}], took: [{}]",
                executionId, bulkRequest.operations().size(), bulkResponse.errors(), bulkResponse.took());
        if (bulkResponse.errors()) {
            List<QueueableSearchable> queueableSearchableList = handleFailedBulkProcess(bulkRequest, bulkResponse);
            for (QueueableSearchable queueableSearchable: queueableSearchableList) {
                if (executionId == -1) {
                    indexerQueueService.addToIndexingQueue(new QueueMessage(requestDetailsProvider.get(),
                            queueableSearchable.getRequestType().toString(), queueableSearchable));
                } else {
                    indexerQueueService.addToIndexingQueue(new QueueMessage("BulkIngester",
                            "BulkIngester-" + executionId,
                            queueableSearchable.getRequestType().toString(), queueableSearchable));

                }

            }
        }
    }

    /**
     * Collect the items of bulk request result which have failed and can be retried
     */
    protected List<QueueableSearchable> handleFailedBulkProcess(BulkRequest bulkRequest,
            BulkResponse bulkResponse) {
        if (!bulkResponse.errors()) {
            return new ArrayList<>();
        }
        List<QueueableSearchable> bulkQueueableSearchableList = new ArrayList<>();
        List<BulkResponseItem> bulkItemResponse = bulkResponse.items();
        for (int i = 0; i < bulkItemResponse.size(); i++) {

            BulkResponseItem item = bulkItemResponse.get(i);
            if (item.error() != null) {
                ErrorCause errorCause = item.error();
                String errorReason = errorCause.reason();
                if (exceptionIsToBeRetried(errorCause)) {
                    BulkOperation bulkOperation = bulkRequest.operations().get(i);
                    QueueableSearchable queueableSearchable = buildQueueableSearchable(bulkOperation);
                    bulkQueueableSearchableList.add(queueableSearchable);
                    LOGGER.info("Request failed, to be retried, failureMessage:[{}], transportSearchable:[{}]",
                            errorReason, queueableSearchable);
                } else {
                    LOGGER.info("Request failed, no retry, failureMessage:[{}]", errorReason);
                }
            }
        }
        LOGGER.debug("Sending bulk failure to queue, initial size:[{}], retry size:[{}]", bulkRequest.operations().size(),
                bulkQueueableSearchableList.size());

        return bulkQueueableSearchableList;
    }

    public long count(Class<? extends Searchable> searchableClazz) {
        return count(searchableClazz, List.of());
    }

    public long count(Class<? extends Searchable> searchableClazz, Collection<SearchCriteria> searchCriteriaList) {
        String indexName = indexNameService.getIndexNamePrefixFromClass(searchableClazz, false);

        CountRequest countRequest = new CountRequest.Builder()
                .index(List.of(indexName))
                .query(buildQuery(searchCriteriaList)._toQuery()).build();

        try {
            CountResponse countResponse = getApiClient().count(countRequest);
            return countResponse.count();
        } catch (IOException | IllegalStateException ex) {
            LOGGER.error("Failed to count entries for index {} because of {}", indexName,
                    ex.getCause());
            return 0L;
        }
    }

    @Override
    public SearchResult fetch(String index, SearchQuery parameters) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(index);
        builder.size(parameters.getMaxResults());
        builder.query(QueryBuilders.matchAll().build()._toQuery());
        if (parameters.getTimeout().isPresent()) {
            builder.timeout(parameters.getTimeout().get().toString());
        }

        if (!parameters.getFilters().isEmpty()) {
            builder.query(buildQuery(parameters.getFilters())._toQuery());
        }

        if (parameters.getFields().isPresent()) {
            if (parameters.getFields().get().isEmpty()) {
                //If no fields are requested, then the result doesn't need any hits
                builder.size(0);

            } else {
                builder.source(
                        new SourceConfig.Builder()
                                .filter(new SourceFilter.Builder()
                                                .includes(parameters.getFields().get())
                                                .build())
                                .build());
            }
        }

        if (!parameters.getSorting().isEmpty()) {
            List<SortOptions> sortOptions = new ArrayList<>();
            for (Map.Entry<String, Boolean> sorting : parameters.getSorting().entrySet()) {
                // If the sorting field is of type string we need make sure that we are searching on a keyword field.
                // Text won't work
                sortOptions.add(
                        new SortOptions.Builder()
                                .field(new FieldSort.Builder()
                                        .field(sorting.getKey())
                                        .order(sorting.getValue().booleanValue() ? SortOrder.Asc : SortOrder.Desc)
                                        .build()
                                ).build()
                );
            }
            builder.sort(sortOptions);
        }
        builder.aggregations(buildAggregations(parameters.getAggregations()));
        SearchRequest searchRequest = builder.build();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing search on index {} with parameters {}, \"builder\": {{}}", index, parameters, JsonUtil.toJson(builder));
        }

        try {
            SearchResponse<ObjectNode> response = getApiClient().search(searchRequest, ObjectNode.class);
            return parseSearchResponse(parameters, response);
        } catch (ElasticsearchException ex) {
            throw new ConfigurationException(ex, EX_CONFIG_SEARCHING, parameters.getMaxResults(), index, ex.getMessage());
        } catch (IOException ex) {
            throw new CommonRuntimeException(ex, EX_IO_SEARCHING, parameters.getMaxResults(), index, ex.getMessage());
        }
    }

    protected BoolQuery buildQuery(Collection<SearchCriteria> searchFilters) {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        List<Query> queryList = new ArrayList<>();
        for (SearchCriteria filter : searchFilters) {
            // all filters are added with AND (means must)
            queryList.add(buildInnerQuery(filter));
        }
        boolQuery.must(queryList);
        return boolQuery.build();
    }

    protected Query buildInnerQuery(SearchCriteria filter) {
        return switch (filter) {
            case DateSearchCriteria criteria -> buildDateQuery(criteria);
            case RangeBasedSearchCriteria criteria -> buildRangeRangeBasedQuery(criteria);
            case ExplicitSearchCriteria criteria -> buildExplicitSearchQuery(criteria);
            case ContainsSearchCriteria criteria -> buildContainsSearchQuery(criteria);
            case ExistingFieldSearchCriteria criteria -> buildExistingFieldQuery(criteria);
            case CombinedSearchCriteria criteria -> buildCombinedQuery(criteria);
            default -> throw new IllegalStateException("Criteria not supported " + filter.getClass().getName());
        };
    }

    protected Query buildDateQuery(DateSearchCriteria criteria) {
        RangeQuery.Builder rangeQuery = buildBasicRangeQuery(criteria);
        if (criteria.getTimeZone() != null) {
            rangeQuery.timeZone(criteria.getTimeZone());
        }
        return searchQueryNot(criteria, rangeQuery.build()._toQuery());
    }

    protected Query buildRangeRangeBasedQuery(RangeBasedSearchCriteria criteria) {
        return searchQueryNot(criteria, buildBasicRangeQuery(criteria).build()._toQuery());
    }

    protected RangeQuery.Builder buildBasicRangeQuery(RangeBasedSearchCriteria criteria) {
        RangeQuery.Builder rangeQuery = co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.range();
        rangeQuery.field(criteria.getFieldName());
        if (criteria.getFrom() != null) {
            rangeQuery.from(criteria.getFrom().toString());
        }

        if (criteria.getTo() != null) {
            rangeQuery.to(criteria.getTo().toString());
        }

        return rangeQuery;
    }

    protected Query buildExplicitSearchQuery(ExplicitSearchCriteria criteria) {
        if (criteria.getFieldValue() instanceof String) {
            String fieldValue = criteria.getFieldValue().toString();

            if (criteria.getAllowWildcards() && (fieldValue.contains("*") || fieldValue.contains("?"))) {
                // wildcard
                WildcardQuery.Builder queryBuilder = QueryBuilders.wildcard()
                        .field(criteria.getFieldName())
                        .value(fieldValue);
                return searchQueryNot(criteria, queryBuilder.build()._toQuery());
            }
        }

        TermQuery.Builder term = QueryBuilders.term()
                .field(criteria.getFieldName())
                .value(objectToFieldValue(criteria.getFieldValue()));

        return searchQueryNot(criteria, term.build()._toQuery());
    }

    protected Query buildContainsSearchQuery(ContainsSearchCriteria criteria) {
        CombinedSearchCriteria combinedSearchCriteria = new CombinedSearchCriteria(Association.OR);
        criteria.getFieldValues().forEach(
                value -> combinedSearchCriteria.addCriteria(new ExplicitSearchCriteria(criteria.getFieldName(), value)));

        return searchQueryNot(criteria, buildCombinedQuery(combinedSearchCriteria));
    }

    protected Query buildExistingFieldQuery(ExistingFieldSearchCriteria criteria) {
        ExistsQuery.Builder existsQuery = QueryBuilders.exists().field(criteria.getFieldName());

        if (criteria.getExists() ^ criteria.isNot()) { //Exclusive or: either A or B but not both.
            return existsQuery.build()._toQuery();
        } else {
            return QueryBuilders.bool().mustNot(existsQuery.build()._toQuery()).build()._toQuery();
        }
    }

    protected Query buildCombinedQuery(CombinedSearchCriteria criteria) {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        for (SearchCriteria searchCriteria : criteria.getSearchCriteriaList()) {
            if (criteria.isAnd()) {
                boolQuery.must(buildInnerQuery(searchCriteria));
            } else {
                boolQuery.should(buildInnerQuery(searchCriteria));
            }
        }
        return boolQuery.build()._toQuery();
    }

    protected Query searchQueryNot(FieldSearchCriteria fieldSearchCriteria, Query query) {
        if (fieldSearchCriteria.isNot()) {
            return QueryBuilders.bool().mustNot(query).build()._toQuery();
        } else {
            return query;
        }
    }

    protected FieldValue objectToFieldValue(Object o) {
        return switch (o) {
            case String string -> FieldValue.of(string);
            case Integer integer -> FieldValue.of(integer);
            case Long longVal -> FieldValue.of(longVal);
            case Float floatVal -> FieldValue.of(floatVal);
            case Double doubleVal -> FieldValue.of(doubleVal);
            case Boolean bool -> FieldValue.of(bool);
            case Date date -> FieldValue.of(date.getTime());
            case null -> FieldValue.NULL;
            default -> throw new IllegalStateException("ExplicitSearchCriteria unexpected value class: " + o.getClass().getName());
        };
    }

    protected Map<String, Aggregation> buildAggregations(List<SearchAggregation> aggregations) {
        Map<String, Aggregation> aggregationMap = new TreeMap<>();
        for (SearchAggregation aggregation : aggregations) {
            aggregationMap.put(aggregation.getName(), buildAggregation(aggregation));
        }
        return aggregationMap;
    }

    protected Aggregation buildAggregation(SearchAggregation aggregation) {
        return switch (aggregation) {
            case SimpleFieldAggregation simpleFieldAgg -> buildAggregation(simpleFieldAgg);
            case CriteriaAggregation criteriaAggregation && !criteriaAggregation.getSearchCriteriaMap().isEmpty() ->
                    buildAggregation(criteriaAggregation);
            case TermAggregation termAggregation -> buildAggregation(termAggregation);
            case BucketScriptAggregation bucketScriptAggregation -> buildAggregation(bucketScriptAggregation);
            case null -> throw new IllegalStateException("SearchAggregation unsupported class: null");
            case default -> throw new IllegalStateException("SearchAggregation unsupported class: " + aggregation.getClass().getName());
        };
    }

    protected Aggregation buildAggregation(SimpleFieldAggregation agg) {
        return switch (agg.getType()) {
            case MAX -> AggregationBuilders.max(val -> val.field(agg.getFieldName()));
            case AVG -> AggregationBuilders.avg(val -> val.field(agg.getFieldName()));
            case MIN -> AggregationBuilders.min(val -> val.field(agg.getFieldName()));
            case SUM -> AggregationBuilders.sum(val -> val.field(agg.getFieldName()));
            case CARDINALITY -> AggregationBuilders.cardinality(val -> val.field(agg.getFieldName()));
            default -> AggregationBuilders.valueCount(val -> val.field(agg.getFieldName()));
        };
    }

    protected Aggregation buildAggregation(BucketScriptAggregation agg) {
        return new Aggregation.Builder().bucketScript(
                val -> val.script(script -> script.inline(inline -> inline.source(agg.getScript())))
                        .bucketsPath(path -> path.dict(agg.getPath()))).build();
    }

    protected Aggregation buildAggregation(CriteriaAggregation agg) {
        Map<String, Query> filterMap = new TreeMap<>();
        for (Map.Entry<String, SearchCriteria> entry: agg.searchCriteriaMap().entrySet()) {
            filterMap.put(entry.getKey(), buildInnerQuery(entry.getValue()));
        }
        FiltersAggregation filtersAggregate = AggregationBuilders.filters().filters(new Buckets.Builder<Query>().keyed(filterMap).build()).build();

        if (agg.getAggregation().isEmpty()) {
            return filtersAggregate._toAggregation();
        }

        return new Aggregation.Builder().filters(filtersAggregate)
                .aggregations(agg.getAggregation().get().getName(), buildAggregation(agg.getAggregation().get())).build();
    }

    protected Aggregation buildAggregation(TermAggregation agg) {
        TermsAggregation.Builder termsAggregation = AggregationBuilders.terms().field(agg.getFieldName());
        if (agg.getMaxResult().isPresent()) {
            termsAggregation.size(agg.getMaxResult().get());
        }
        if (agg.getPartition().isPresent()) {
            termsAggregation.include(include -> include.partition(partition -> partition
                    .partition(agg.getPartition().get().partition())
                    .numPartitions(agg.getPartition().get().numPartition())));
        }

        Map<String, Aggregation> aggregationMap = agg.aggregations().stream().collect(
                Collectors.toMap(SearchAggregation::getName, this::buildAggregation));

        if (agg.getOrder().isPresent()) {
            aggregationMap.put("sortByBucketSort", new Aggregation.Builder().bucketSort(bucket ->
                    bucket.sort(List.of(SortOptions.of(sortOption -> sortOption.field(field ->
                            field.field(agg.getOrder().get().aggregationToOrderAfter()).order(
                                    agg.getOrder().get().asc() ? SortOrder.Asc : SortOrder.Desc)))))).build());
        }

        return new Aggregation.Builder().terms(termsAggregation.build()).aggregations(aggregationMap).build();
    }


    protected SearchResult parseSearchResponse(SearchQuery parameters, SearchResponse<ObjectNode> response) {
        return new SearchResult(
                response.hits().total() != null ? response.hits().total().value() : -1,
                response.hits().maxScore() != null ? response.hits().maxScore().doubleValue() : -1,
                response.took(),
                Boolean.TRUE.equals(response.terminatedEarly()),
                response.timedOut(),
                response.scrollId(),
                parseHits(response.hits(), parameters.getOnlySource()),
                parseAggregations(response.aggregations()),
                TotalHitsRelation.Gte.equals(response.hits().total().relation()));
    }

    protected List<JsonNode> parseHits(HitsMetadata<ObjectNode> hitsMetadata, boolean onlySource) {
        List<JsonNode> hitList = new ArrayList<>();
        for (Hit<ObjectNode> hit : hitsMetadata.hits()) {
            if (onlySource) {
                hitList.add(hit.source());
            } else {
                hitList.add(hit.source()); //TODO GET METADATA
            }
        }
        return hitList;
    }

    protected Map<String, AggregationResult> parseAggregations(Map<String, Aggregate> aggregations) {
        Map<String, AggregationResult> aggregationResults = new HashMap<>();

        if (aggregations != null) {
            for (Map.Entry<String, Aggregate> agg : aggregations.entrySet()) {
                aggregationResults.put(agg.getKey(), parseAggregation(agg.getValue(), agg.getKey()));
            }
        }
        return aggregationResults;
    }

    protected AggregationResult parseAggregation(Aggregate agg, String key) {
        AggregateVariant variant = (AggregateVariant) agg._get();

        return switch (variant) {
            case SingleMetricAggregateBase singleValue -> new SimpleAggregationResult(key, singleValue.value());
            case FiltersAggregate filtersAggregate ->
                    parseFilterAggregation(filtersAggregate, filtersAggregate.buckets(), key);
            case TermsAggregateBase<?> termsAggregateBase -> parseTermAggregation(termsAggregateBase, key);
            case CardinalityAggregate cardinalityAggregate ->
                    new SimpleAggregationResult(key, cardinalityAggregate.value());
            case null -> throw new IllegalStateException("Unsupported aggregations variant received: null");
            case default ->
                    throw new IllegalStateException("Unsupported aggregations variant received:" + variant._aggregateKind());
        };
    }

    protected AggregationResult parseFilterAggregation(FiltersAggregate filtersAggregate,
            Buckets<FiltersBucket> buckets, String key) {
        if (!Buckets.Kind.Keyed.equals(filtersAggregate.buckets()._kind())) {
            throw new IllegalStateException("Unsupported filter value type: " +filtersAggregate.buckets()._kind());
        }
        Map<String, Object> result = new HashMap<>(buckets.keyed().size());
        for (Map.Entry<String, FiltersBucket> bucket: buckets.keyed().entrySet()) {
            for (Map.Entry<String, Aggregate> aggregate: bucket.getValue().aggregations().entrySet()) {
                if (aggregate.getValue()._get() instanceof SingleMetricAggregateBase singleValue) {
                    result.put(bucket.getKey(), singleValue.value());
                } else {
                    throw new IllegalStateException("Unsupported filter value type: " + aggregate.getValue()._kind());
                }
            }
        }
        return new CriteriaAggregationResult(key, result);
    }

    protected AggregationResult parseTermAggregation(TermsAggregateBase<?> termsAggregateBase, String key) {
        if (!Buckets.Kind.Array.equals(termsAggregateBase.buckets()._kind())) {
            throw new IllegalStateException("Unsupported filter value type: " +termsAggregateBase.buckets()._kind());
        }
        List<TermAggregationResult.Bucket> resultBuckets = new LinkedList<>();
        for (Object bucket: termsAggregateBase.buckets().array()) {
            if (bucket instanceof StringTermsBucket stringTermsAggregate) {
                Map<String, Object> aggregationResult = new LinkedHashMap<>();
                for (Map.Entry<String, Aggregate> agg : stringTermsAggregate.aggregations().entrySet()) {
                    aggregationResult.put(agg.getKey(), parseAggregation(agg.getValue(), agg.getKey()));
                }
                resultBuckets.add(new TermAggregationResult.Bucket(stringTermsAggregate.key().stringValue() ,aggregationResult));
            }
        }
        return new TermAggregationResult(key, resultBuckets);
    }

    protected void addToBulkProcessor(BulkOperation bulkOperation) {
        try {
            getBulkIngester().add(bulkOperation);
        } catch (IllegalStateException ex) {
            LOGGER.info("Error while adding to BulkProcessor, message: {}", ex.getMessage());
            QueueableSearchable searchable = buildQueueableSearchable(bulkOperation);
            indexerQueueService.addToIndexingQueue(new QueueMessage(requestDetailsProvider.get(),
                    searchable.getRequestType().toString(), searchable));
        }
    }

    protected IndexRequest<Object> buildIndexRequest(Searchable searchable) {
        String indexName = indexNameService.getIndexName(searchable);
        IndexRequest.Builder<Object> indexRequest = new IndexRequest.Builder<>()
                .index(indexName)
                .withJson(new StringReader(searchable.getObjectNode().toString()))
                .id(searchable.getBusinessId());

        if (searchable.getVersion() != null) {
            indexRequest.version(searchable.getVersion()).versionType(VersionType.ExternalGte);
        }
        return indexRequest.build();
    }

    protected BulkOperation buildIndexOperation(Searchable searchable) {
        String indexName = indexNameService.getIndexName(searchable);
        IndexOperation.Builder<Object> indexRequest = new IndexOperation.Builder<>()
                .index(indexName)
                .document(parseObjectNode(searchable.getObjectNode()))
                .id(searchable.getBusinessId());

        if (searchable.getVersion() != null) {
            indexRequest.version(searchable.getVersion()).versionType(VersionType.ExternalGte);
        }
        return new BulkOperation(indexRequest.build());
    }

    protected BulkOperation buildIndexOperation(QueueableSearchable queueableSearchable) {
        IndexOperation.Builder<Object> operation = new IndexOperation.Builder<>()
                .index(queueableSearchable.getIndex())
                .id(queueableSearchable.getId())
                .routing(queueableSearchable.getRouting())
                .document(parseObjectNode(JsonUtil.fromJson(queueableSearchable.getJsonSource())));
        if (queueableSearchable.getVersion() != null && !StringUtils.isEmpty(queueableSearchable.getId())) {
            operation.version(queueableSearchable.getVersion()).versionType(VersionType.ExternalGte);
        }
        return new BulkOperation(operation.build());
    }

    protected UpdateRequest<Object, Object> buildUpdateRequest(Searchable searchable, boolean upsert) {
        String indexName = indexNameService.getIndexName(searchable);

        UpdateRequest.Builder<Object, Object> updateRequest = new UpdateRequest.Builder<>()
                .index(indexName)
                .id(searchable.getBusinessId())
                .withJson(new StringReader(searchable.getObjectNode().toString()))
                .docAsUpsert(upsert);

        return updateRequest.build();
    }

    protected BulkOperation buildUpdateOperation(QueueableSearchable queueableSearchable) {
        UpdateAction.Builder<Object, Object> action = new UpdateAction.Builder<>()
                .docAsUpsert(queueableSearchable.getUpsert())
                .withJson(new StringReader(queueableSearchable.getJsonSource()));

        UpdateOperation.Builder<Object, Object> updateRequest = new UpdateOperation.Builder<>()
                .index(queueableSearchable.getIndex())
                .id(queueableSearchable.getId())
                .withJson(new StringReader(queueableSearchable.getJsonSource()))
                .action(action.build());
        if (queueableSearchable.getVersion() != null && !StringUtils.isEmpty(queueableSearchable.getId())) {
            updateRequest.version(queueableSearchable.getVersion()).versionType(VersionType.ExternalGte);
        }
        return new BulkOperation(updateRequest.build());
    }

    protected BulkOperation buildUpdateOperation(Searchable searchable, boolean upsert) {
        String indexName = indexNameService.getIndexName(searchable);

        UpdateAction.Builder<Object, Object> updateAction = new UpdateAction.Builder<>()
                .doc(parseObjectNode(searchable.getObjectNode()))
                .docAsUpsert(upsert);

        UpdateOperation.Builder<Object, Object> updateRequest = new UpdateOperation.Builder<>()
                .index(indexName)
                .id(searchable.getBusinessId())
                .action(updateAction.build());

        return new BulkOperation(updateRequest.build());
    }

    public void reload() {
        closeBulkIngester();
        connection.reloadConfig();
        connection.disconnect();
        connection.connect();
    }

    @Override
    public boolean enabled() {
        return connection.enabled();
    }

    protected void disconnect() {
        connection.disconnect();
    }

    /**
     * In some situations the underlying http client stops proceeding and
     * returns IllegalStateException.
     *
     * @param ex the exception to check
     * @return true if the clients has reconnected
     */
    protected boolean reconnectClientIfNeeded(IllegalStateException ex) {
        if (Arrays.stream(FILTER_HTTPCLIENT_MESSAGES).anyMatch(ex.getMessage()::contains)) {
            LOGGER.info("IllegalStateException reconnectClient");
            reload();
            return true;
        }
        return false;
    }

    /**
     * Shutdown and close bulk ingester
     */
    protected void closeBulkIngester() {
        // make sure all bulk requests have been processed
        try {
            if (bulkIngester != null) {
                bulkIngester.close();
            }
        } catch (Exception ex) {
            LOGGER.warn("exception while closing bulkProcessor, error: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
        bulkIngester = null;
    }

    protected BulkIngester.Builder<Object> createBulkRequestBuilder(BulkListener<Object> listener) {
        BulkIngester.Builder<Object> builder = new BulkIngester.Builder<>()
                .client(connection.getApiClient())
                .listener(listener);


        long flushInterval = configService.get(FLUSH_INTERVAL_CONFIG).asInt().orElse(10);
        LOGGER.info("BulkIngester.Builder FlushInterval: {}", flushInterval);
        builder.flushInterval(flushInterval, TimeUnit.SECONDS);

        int bulkAction = configService.get(BULK_ACTION_CONFIG).asInt().orElse(2500);
        LOGGER.info("BulkIngester.Builder BulkActions: {}", bulkAction);
        builder.maxOperations(bulkAction);

        int concurrentRequests = configService.get(CONCURRENT_REQUEST_CONFIG).asInt().orElse(3);
        LOGGER.info("BulkIngester.Builder ConcurrentRequests: {}", concurrentRequests);
        builder.maxConcurrentRequests(concurrentRequests);

        int bulkSize = configService.get(BULK_SIZE).asInt().orElse(10);
        LOGGER.info("BulkIngester.Builder BulkSize: {}", bulkSize);
        builder.maxSize(bulkSize * BYTES_IN_MB);

        return builder;
    }

    /**
     * Checks the message of the exception if its an {@link ElasticsearchException} otherwise it will be retried
     */
    public boolean exceptionIsToBeRetried(Exception ex) {
        //TODO FIND IT OUT
        LOGGER.error("An error occurred during an elastic operation", ex);
        return true;
    }

    public boolean exceptionIsToBeRetried(ErrorCause errorCause) {
        //TODO FIND IT OUT
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("An error occurred during an elastic operation {}", JsonUtil.toJson(errorCause));
        }
        return true;
    }

    protected QueueableSearchable buildQueueableSearchable(BulkOperation operation) {
        return switch (operation._get()) {
            case UpdateOperation<?, ?> updateRequest -> buildQueueableSearchable(updateRequest);
            case IndexOperation<?> indexRequest -> buildQueueableSearchable(indexRequest);
            case DeleteOperation deleteRequest -> buildQueueableSearchable(deleteRequest);
            default -> throw new IllegalArgumentException("Unknown request type: " + operation._get().getClass().getName());
        };
    }

    protected QueueableSearchable buildQueueableSearchable(IndexOperation<?> request) {
        return new QueueableSearchable(request.index(), request.id(), request.version(), request.routing(),
                request.document().toString(), QueueableSearchable.RequestType.INDEX);
    }

    protected QueueableSearchable buildQueueableSearchable(UpdateOperation<?,?> request) {
        return new QueueableSearchable(request.index(), request.id(), null, request.routing(),
                request.action().doc().toString(),
                Boolean.TRUE.equals(request.action().docAsUpsert()),
                QueueableSearchable.RequestType.UPDATE);

    }

    protected QueueableSearchable buildQueueableSearchable(DeleteOperation request) {
        return new QueueableSearchable(request.index(), request.id(), request.version(), request.routing(), null,
                QueueableSearchable.RequestType.DELETE);
    }

    protected JsonData parseObjectNode(JsonNode objectNode) {
        JsonpMapper jsonpMapper = getApiClient()._transport().jsonpMapper();
        JsonProvider jsonProvider = jsonpMapper.jsonProvider();

        return JsonData.from(jsonProvider.createParser(new StringReader(objectNode.toString())), jsonpMapper);
    }

    protected List<String> getIndicesOfSearchable(Class<? extends Searchable> searchableClazz) {
        String searchableIndexName = indexNameService.getIndexNamePrefixFromClass(searchableClazz, true);
        List<String> searchableIndices = new ArrayList<>();
        for (String indexName: getAllIndices()) {
            if (indexName.startsWith(searchableIndexName.concat("-")) || indexName.equals(searchableIndexName)) {
                searchableIndices.add(indexName);
            }
        }
        return searchableIndices;
    }

    protected Set<String> getAllIndices() {
        GetIndexRequest request = new GetIndexRequest.Builder().index("*").build();

        try {
            return getApiClient().indices().get(request).result().keySet();
        } catch (IOException ex) {
            LOGGER.error("Unable to retrieve all indices", ex);
        }
        return Set.of();
    }
}
