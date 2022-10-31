package com.neo.util.framework.elastic.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.enumeration.Synchronization;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.api.persistence.aggregation.*;
import com.neo.util.framework.api.persistence.criteria.*;
import com.neo.util.framework.api.persistence.search.*;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionRepository;
import com.neo.util.framework.elastic.api.IndexNamingService;
import jakarta.inject.Provider;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class provides methods to interact with elastic search
 */
@SuppressWarnings("deprecation")
@Alternative
@Priority(PriorityConstants.APPLICATION)
@ApplicationScoped
public class ElasticSearchRepository implements SearchRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepository.class);

    protected static final List<RestStatus> FILTER_REST_STATUS = Arrays.asList(RestStatus.CONFLICT,
            RestStatus.NOT_FOUND);

    //Response looks like this: I/O reactor status: STOPPED","I/O reactor has been shut down"
    protected static final String[] FILTER_HTTPCLIENT_MESSAGES = new String[] { "I/O reactor" };

    protected static final String[] FILTER_MESSAGES = new String[] { "type=version_conflict_engine_exception" };

    protected static final String TYPE = "type";
    protected static final String PROPERTIES = "properties";

    protected static final String CONFIG_PREFIX = "elastic";

    public static final String FLUSH_INTERVAL_CONFIG = CONFIG_PREFIX + ".FlushInterval";
    public static final String BULK_ACTION_CONFIG = CONFIG_PREFIX + ".BulkActions";
    public static final String CONCURRENT_REQUEST_CONFIG = CONFIG_PREFIX + ".ConcurrentRequests";
    public static final String BULK_SIZE = CONFIG_PREFIX + ".BulkSize";

    protected static final ExceptionDetails EX_SYNCHRONOUS_INDEXING = new ExceptionDetails(
            "elk/synchronous-indexing", "IOException while synchronous indexing", true);

    @Inject
    protected ConfigService configService;

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @Inject
    protected IndexingQueueService indexerQueueService;

    @Inject
    protected IndexNamingService indexNameService;

    @Inject
    protected ElasticSearchConnectionRepository connection;

    protected volatile BulkProcessor bulkProcessor;

    protected synchronized void setupBulkProcessor() {
        if (bulkProcessor != null) {
            return;
        }

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest bulkRequest) {
                LOGGER.debug("Executing bulk [{}] with [{}] requests", executionId, bulkRequest.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                handleBulkResponse(executionId, bulkRequest, bulkResponse);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, Throwable failure) {
                LOGGER.info("Executed bulk [{}] with complete failure, entire bulk will be retried, message: [{}]", executionId,
                        failure.getMessage());
                ArrayList<QueueableSearchable> bulkQueueableSearchableList = new ArrayList<>();
                for (DocWriteRequest<?> request : bulkRequest.requests()) {
                    bulkQueueableSearchableList.add(generateQueueableSearchable(request));
                }
                indexerQueueService.addToIndexingQueue(new QueueMessage(requestDetailsProvider.get(),
                        QueueableSearchable.RequestType.BULK.toString(), bulkQueueableSearchableList));
                if (failure instanceof IllegalStateException illegalStateException) {
                    reconnectClientIfNeeded(illegalStateException);
                }
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

    public void connectionStatusListener(@Observes ElasticSearchConnectionStatusEvent event) {
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

    protected ElasticsearchClient getApiClient() {
        return connection.getApiClient();
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
                throw new CommonRuntimeException(ex, EX_SYNCHRONOUS_INDEXING);
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
                throw new CommonRuntimeException(ex, EX_SYNCHRONOUS_INDEXING, ex);
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
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void update(List<? extends Searchable> searchableList, boolean partial) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void delete(Searchable searchable) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void delete(List<? extends Searchable> searchableList) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void deleteAll(Class<? extends Searchable> searchableClazz) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void process(QueueableSearchable queueableSearchable) {
        switch (queueableSearchable.getRequestType()) {
        case INDEX -> getBulkProcessor().add(generateIndexRequest(queueableSearchable));
        case UPDATE, DELETE -> throw new IllegalStateException("Not implemented yet");
        default -> throw new IllegalStateException("Not supported");
        }
    }

    @Override
    public void process(List<QueueableSearchable> transportSearchableList) {
        BulkRequest bulkRequest = new BulkRequest();
        for (QueueableSearchable queueableSearchable : transportSearchableList) {
            switch (queueableSearchable.getRequestType()) {
            case INDEX -> bulkRequest.add(generateIndexRequest(queueableSearchable));
            case UPDATE, DELETE -> throw new IllegalStateException("Not implemented yet");
            default -> throw new IllegalStateException("Not supported");
            }
        }

        try {
            BulkResponse bulkResponse = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
            handleBulkResponse(-1,bulkRequest, bulkResponse);
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
        LOGGER.info("Executed bulk [{}] with [{}] requests, hasFailures: [{}], took: [{}], ingestTook: [{}]",
                executionId, bulkRequest.numberOfActions(), bulkResponse.hasFailures(), bulkResponse.getTook(),
                bulkResponse.getIngestTook());
        if (bulkResponse.hasFailures()) {
            List<QueueableSearchable> queueableSearchableList = handleFailedBulkProcess(bulkRequest, bulkResponse);
            for (QueueableSearchable queueableSearchable: queueableSearchableList) {
                indexerQueueService.addToIndexingQueue(new QueueMessage(requestDetailsProvider.get(),
                        queueableSearchable.getRequestType().toString(), queueableSearchable));
            }
        }
    }

    /**
     * Collect the items of bulk request result which have failed and can be retried
     */
    protected ArrayList<QueueableSearchable> handleFailedBulkProcess(BulkRequest bulkRequest,
            BulkResponse bulkResponse) {
        if (!bulkResponse.hasFailures()) {
            return new ArrayList<>();
        }
        ArrayList<QueueableSearchable> bulkQueueableSearchableList = new ArrayList<>();
        BulkItemResponse[] bulkItemResponse = bulkResponse.getItems();
        for (int i = 0; i < bulkItemResponse.length; i++) {

            BulkItemResponse item = bulkItemResponse[i];
            if (item.isFailed()) {
                BulkItemResponse.Failure failure = item.getFailure();
                if (exceptionIsToBeRetried(failure.getCause())) {
                    DocWriteRequest<?> request = bulkRequest.requests().get(i);
                    QueueableSearchable transportSearchable = generateQueueableSearchable(request);
                    bulkQueueableSearchableList.add(transportSearchable);
                    LOGGER.info("Request failed, to be retried, failureMessage:[{}], transportSearchable:[{}]",
                            failure.getMessage(), transportSearchable);
                } else {
                    LOGGER.info("Request failed, no retry, failureMessage:[{}]", failure.getMessage());
                }
            }
        }
        LOGGER.debug("Sending bulk failure to queue, initial size:[{}], retry size:[{}]", bulkRequest.numberOfActions(),
                bulkQueueableSearchableList.size());

        return bulkQueueableSearchableList;
    }

    @Override
    public SearchResult fetch(String index, SearchQuery parameters) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(index);
        builder.size(parameters.getMaxResults());
        builder.query(QueryBuilders.matchAll().build()._toQuery());
        if (parameters.getTimeout().isPresent()) {
            builder.timeout(new TimeValue(parameters.getTimeout().get(), TimeUnit.MILLISECONDS).toString());
        }

        addSearchFilters(parameters.getFilters(), builder);
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
            Map<String, Class<?>> mapping = getFlatTypeMapping(readTypeMapping(index));
            for (Map.Entry<String, Boolean> sorting : parameters.getSorting().entrySet()) {
                // if the sorting field is of type string we need to sort on the keyword
                // property of that field
                String fieldName = sorting.getKey();
                if (mapping.get(sorting.getKey()).isAssignableFrom(String.class)) {
                    fieldName = fieldName.concat(Searchable.INDEX_SEARCH_KEYWORD);
                }
                sortOptions.add(
                        new SortOptions.Builder()
                                .field(new FieldSort.Builder()
                                        .field(fieldName)
                                        .order(sorting.getValue().booleanValue() ? SortOrder.Asc : SortOrder.Desc)
                                        .build()
                                ).build()
                );
            }
            builder.sort(sortOptions);
        }

        addAggregations(parameters.getAggregations(), builder);

        LOGGER.debug("Executing search on index {} with parameters {}, builder {}", index, parameters, builder);

        try {
            SearchRequest searchRequest = builder.build();
            SearchResponse<ObjectNode> response = getApiClient().search(searchRequest, ObjectNode.class);
            return parseSearchResponse(parameters, response);
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Failed to fetch {} entries for index {} because of {}", parameters.getMaxResults(), index,
                    e.getCause());
            return new SearchResult();
        }
    }


    protected Map<String, Object> readTypeMapping(String index) {
        /*
        --NOTE--
        If I want to fully migrate of from HighLevel RestClient I would need to get the mapping another way.

        This blog post describes getting the mapping manually with a request using the RestClient
        https://spinscale.de/posts/2022-03-03-running-the-elasticcc-platform-part-2.html#testing
         */
        Map<String, Object> results = new HashMap<>();
        GetMappingsRequest request = new GetMappingsRequest();
        request.setMasterTimeout(TimeValue.timeValueMinutes(1));
        request.indices(index);

        try {
            GetMappingsResponse response = getClient().indices().getMapping(request, RequestOptions.DEFAULT);
            Map<String, MappingMetadata> mappings = response.mappings();

            for (Map.Entry<String, MappingMetadata> mapping : mappings.entrySet()) {
                Map<String, Object> properties = (Map<String, Object>) mapping.getValue().getSourceAsMap().get(PROPERTIES);
                if (properties == null) {
                    continue;
                }
                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    if (property.getValue() instanceof Map) {
                        if (results.containsKey(property.getKey())) {
                            Object typeMapping = results.get(property.getKey());

                            if (typeMapping instanceof Class) {
                                // this is an explicit field mapping, skip this
                                LOGGER.debug("Skipping properties of key {} because this key was already analyzed",
                                        property.getKey());
                            } else {
                                Map<String, Object> subMap = (Map<String, Object>) analyzePropertyMap(
                                        (Map<String, Object>) property.getValue());
                                Map<String, Object> existingMap = (Map<String, Object>) results.get(property.getKey());
                                existingMap.putAll(subMap);
                            }
                        } else {
                            results.put(property.getKey(),
                                    analyzePropertyMap((Map<String, Object>) property.getValue()));
                        }
                    }
                }
            }
        } catch (IOException | IllegalStateException ex) {
            //TODO handle exceptions properly
            ex.printStackTrace();
        }

        return results;
    }

    protected Object analyzePropertyMap(Map<String, Object> propertyMap) {
        if (propertyMap.containsKey(TYPE)) {
            // explicit property
            return getClassBasedOnTypeName(propertyMap.get(TYPE).toString());
        } else {
            // is an inner map
            Map<String, Object> innerMap = (Map<String, Object>) propertyMap.get(PROPERTIES);
            Map<String, Object> innerTypeMap = new HashMap<>();

            // this loop with the recursive call makes sure we add all the sub properties as
            // a column
            for (Map.Entry<String, Object> innerProperty : innerMap.entrySet()) {
                Map<String, Object> innerPropertyMap = (Map<String, Object>) innerProperty.getValue();

                innerTypeMap.put(innerProperty.getKey(), analyzePropertyMap(innerPropertyMap));
            }

            return innerTypeMap;
        }
    }

    protected Class<?> getClassBasedOnTypeName(String typeName) {
        return switch (typeName) {
            case "string" -> String.class;
            case "boolean" -> Boolean.class;
            case "date" -> Date.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "integer" -> Integer.class;
            case "float" -> Float.class;
            case null, default -> String.class;
        };
    }

    public Map<String, Class<?>> getFlatTypeMapping(Map<String, Object> readTypeMapping) {
        Map<String, Class<?>> result = new HashMap<>();

        getFlatTypeMapping(readTypeMapping, "", result);

        return result;
    }

    /**
     * Recursive implementation of {@link #getFlatTypeMapping(Map)}
     */
    protected void getFlatTypeMapping(Map<String, Object> readTypeMapping, String prefix,
            Map<String, Class<?>> result) {
        for (Map.Entry<String, Object> entry : readTypeMapping.entrySet()) {
            String fieldName = prefix + entry.getKey();

            if (entry.getValue() instanceof Class clazz) {
                result.put(fieldName, clazz);

            } else {
                // currently we support nested fields and the field as a whole, so we just write
                // out the toString
                // which makes this field type string and not sortable
                result.put(fieldName, String.class);
            }

            if (entry.getValue() instanceof Map<?, ?>) {
                // this is a nested field, therefore we need to read the inner map
                getFlatTypeMapping((Map<String, Object>) entry.getValue(), prefix + entry.getKey() + ".", result);
            }
        }
    }

    protected void addSearchFilters(List<SearchCriteria> searchFilters, SearchRequest.Builder builder) {
        if (!searchFilters.isEmpty()) {
            BoolQuery.Builder boolQuery = co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.bool();
            List<Query> queryList = new ArrayList<>();
            for (SearchCriteria filter : searchFilters) {
                // all filters are added with AND (means must)
                queryList.add(buildInnerQuery(filter));
            }
            boolQuery.must(queryList);
            builder.query(boolQuery.build()._toQuery());
        }
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
            //rangeQuery.includeLower(criteria.isIncludeFrom());
        }

        if (criteria.getTo() != null) {
            rangeQuery.to(criteria.getTo().toString());
            //rangeQuery.includeUpper(criteria.isIncludeTo());
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
        CombinedSearchCriteria combinedSearchCriteria = new CombinedSearchCriteria(CombinedSearchCriteria.Association.OR);
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

    protected void addAggregations(List<SearchAggregation> aggregations, SearchRequest.Builder builder) {
        Map<String, Aggregation> aggregationMap = new TreeMap<>();
        for (SearchAggregation aggregation : aggregations) {
             if (aggregation instanceof SimpleFieldAggregation simpleFieldAgg) {
                 aggregationMap.put(simpleFieldAgg.getName(),
                         buildAggregation(
                                 simpleFieldAgg.getType(),
                                 simpleFieldAgg.getFieldName()));
            } else if (aggregation instanceof CriteriaAggregation criteriaAggregation
                     && criteriaAggregation.getSearchCriteriaMap().size() > 1) {
                 Map<String, Query> filterMap = new TreeMap<>();
                 for (Map.Entry<String, SearchCriteria> entry: criteriaAggregation.getSearchCriteriaMap().entrySet()) {
                     filterMap.put(entry.getKey(), buildInnerQuery(entry.getValue()));
                 }
                 FiltersAggregation filtersAggregate = AggregationBuilders.filters().filters(new Buckets.Builder<Query>().keyed(filterMap).build()).build();

                 Aggregation subAggregationMap = buildAggregation(
                         criteriaAggregation.getAggregation().getType(),
                         criteriaAggregation.getAggregation().getFieldName());
                 aggregationMap.put(
                         criteriaAggregation.getName(),
                         new Aggregation.Builder().filters(filtersAggregate).aggregations(criteriaAggregation.getName(), subAggregationMap).build());
            }
        }
        builder.aggregations(aggregationMap);
    }

    protected Aggregation buildAggregation(SimpleFieldAggregation.Type aggregationType, String fieldName) {
        return switch (aggregationType) {
            case MAX -> AggregationBuilders.max(agg -> agg.field(fieldName));
            case AVG -> AggregationBuilders.avg(agg -> agg.field(fieldName));
            case MIN -> AggregationBuilders.min(agg -> agg.field(fieldName));
            case SUM -> AggregationBuilders.sum(agg -> agg.field(fieldName));
            case CARDINALITY -> AggregationBuilders.cardinality(agg -> agg.field(fieldName));
            default -> AggregationBuilders.valueCount(agg -> agg.field(fieldName));
        };
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
                AggregateVariant variant = (AggregateVariant) agg.getValue()._get();
                String key = agg.getKey();

                if (variant instanceof SingleMetricAggregateBase singleValue) {
                    aggregationResults.put(key, new SimpleAggregationResult(key, singleValue.value()));
                } else if (variant instanceof FiltersAggregate filtersAggregate) {
                    parseFilterAggregation(aggregationResults, filtersAggregate, filtersAggregate.buckets(), key);
                } else {
                    LOGGER.warn("Unsupported simpleFieldAggregation variant received [{}]", variant._aggregateKind());
                }
            }
        }
        return aggregationResults;
    }

    protected void parseFilterAggregation(Map<String, AggregationResult> toAddTo,
            FiltersAggregate filtersAggregate, Buckets<FiltersBucket> buckets, String key) {
        switch (filtersAggregate.buckets()._kind()) {
        case Keyed:
            Map<String, Object> result = new HashMap<>(buckets.keyed().size());
            for (Map.Entry<String, FiltersBucket> bucket: buckets.keyed().entrySet()) {
                for (Map.Entry<String, Aggregate> aggregate: bucket.getValue().aggregations().entrySet()) {
                    if (aggregate.getValue()._get() instanceof SingleMetricAggregateBase singleValue) {
                        result.put(bucket.getKey(), singleValue.value());
                    } else {
                        LOGGER.warn("Unsupported simpleFieldAggregation variant received [{}]", aggregate.getValue()._kind());
                    }
                }
            }
            toAddTo.put(key, new CriteriaAggregationResult(key, result));
            break;
        case default:
            LOGGER.warn("Unsupported filter value type [{}]", filtersAggregate.buckets()._kind());
        }
    }

    protected void addToBulkProcessor(DocWriteRequest<?> request) {
        try {
            getBulkProcessor().add(request);
        } catch (IllegalStateException ex) {
            LOGGER.info("Error while adding to BulkProcessor, message: {}", ex.getMessage());
            QueueableSearchable searchable = generateQueueableSearchable(request);
            indexerQueueService.addToIndexingQueue(new QueueMessage(requestDetailsProvider.get(),
                    searchable.getRequestType().toString(), searchable));
        }
    }

    protected IndexRequest generateIndexRequest(Searchable searchable) {
        String indexName = indexNameService.getIndexName(searchable);
        IndexRequest indexRequest = new IndexRequest(indexName)
                .source(parseSearchableToObjectNode(searchable).toString(), XContentType.JSON)
                .id(searchable.getBusinessId());

        if (searchable.getVersion() != null) {
            indexRequest.version(searchable.getVersion()).versionType(VersionType.EXTERNAL_GTE);
        }
        return indexRequest;
    }

    protected ObjectNode parseSearchableToObjectNode(Searchable searchable) {
        try {
            ObjectNode objectNode = searchable.getJsonNode();
            objectNode.put(Searchable.BUSINESS_ID, searchable.getBusinessId());
            objectNode.put(Searchable.TYPE, searchable.getClassName());
            return objectNode;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Error while parsing searchable to json: " + searchable.getClassName() + ":" + searchable.getBusinessId(), e);
        }
    }

    public void reload() {
        closeBulkProcessor();
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
     * In some situations the underlying http client used by elasticsearch's high level rest client stops proceeding and
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
     * prepare shutdown and close bulk processor
     */
    protected void closeBulkProcessor() {
        // make sure all bulk requests have been processed
        try {
            if (bulkProcessor != null) {
                bulkProcessor.awaitClose(30L,
                        TimeUnit.SECONDS);
            }
        } catch (InterruptedException ex) {
            LOGGER.warn("exception while closing bulkProcessor, error: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
        bulkProcessor = null;
    }

    protected BulkProcessor.Builder createBulkRequestBuilder(BulkProcessor.Listener listener) {
        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) ->
                        getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener);


        long flushInterval = configService.get(FLUSH_INTERVAL_CONFIG).asInt().orElse(10);
        LOGGER.info("BulkProcessor.Builder FlushInterval: {}", flushInterval);
        builder.setFlushInterval(TimeValue.timeValueSeconds(flushInterval));

        int bulkAction = configService.get(BULK_ACTION_CONFIG).asInt().orElse(2500);
        LOGGER.info("BulkProcessor.Builder BulkActions: {}", bulkAction);
        builder.setBulkActions(bulkAction);

        int concurrentRequests = configService.get(CONCURRENT_REQUEST_CONFIG).asInt().orElse(3);
        LOGGER.info("BulkProcessor.Builder ConcurrentRequests: {}", concurrentRequests);
        builder.setConcurrentRequests(concurrentRequests);

        int bulkSize = configService.get(BULK_SIZE).asInt().orElse(10);
        LOGGER.info("BulkProcessor.Builder BulkSize: {}", bulkSize);
        builder.setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB));

        return builder;
    }


    protected IndexRequest generateIndexRequest(QueueableSearchable queueableSearchable) {
        IndexRequest request = new IndexRequest(queueableSearchable.getIndex()).id(queueableSearchable.getId())
                .routing(queueableSearchable.getRouting())
                .source(queueableSearchable.getJsonSource(), XContentType.JSON);
        if (queueableSearchable.getVersion() != null && !StringUtils.isEmpty(queueableSearchable.getId())) {
            request.versionType(VersionType.EXTERNAL_GTE).version(queueableSearchable.getVersion());
        }

        return request;
    }

    /**
     * Checks the message of the exception if its an {@link ElasticsearchException} otherwise it will be retried
     */
    public boolean exceptionIsToBeRetried(Exception e) {
        if (e instanceof ElasticsearchException elasticEx) {
            return !FILTER_REST_STATUS.contains(elasticEx.status()) && Arrays.stream(FILTER_MESSAGES)
                    .noneMatch(elasticEx.getDetailedMessage()::contains);
        }
        return true;
    }

    protected QueueableSearchable generateQueueableSearchable(DocWriteRequest<?> request) {
        return switch (request) {
            case UpdateRequest updateRequest -> generateQueueableSearchable(updateRequest);
            case IndexRequest indexRequest -> generateQueueableSearchable(indexRequest);
            case DeleteRequest deleteRequest -> generateQueueableSearchable(deleteRequest);
            default -> throw new IllegalArgumentException("Unknown request type: " + request.getClass().getName());
        };
    }

    protected QueueableSearchable generateQueueableSearchable(IndexRequest request) {
        return new QueueableSearchable(request.index(), request.id(), request.version(), request.routing(),
                request.source().utf8ToString(), QueueableSearchable.RequestType.INDEX);
    }

    protected QueueableSearchable generateQueueableSearchable(UpdateRequest request) {
        return new QueueableSearchable(request.index(), request.id(), request.version(), request.routing(),
                request.doc().source().utf8ToString(),
                request.upsertRequest() != null ? request.upsertRequest().source().utf8ToString() : null,
                QueueableSearchable.RequestType.UPDATE);

    }

    protected QueueableSearchable generateQueueableSearchable(DeleteRequest request) {
        return new QueueableSearchable(request.index(), request.id(), request.version(), request.routing(), null,
                QueueableSearchable.RequestType.DELETE);
    }
}
