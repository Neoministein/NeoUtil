package com.neo.util.framework.elastic.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.enumeration.Synchronization;
import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.persistence.aggregation.*;
import com.neo.util.framework.api.persistence.criteria.*;
import com.neo.util.framework.api.persistence.search.*;
import com.neo.util.framework.api.queue.QueueMessage;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionRepository;
import com.neo.util.framework.elastic.api.IndexNamingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
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
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class provides methods to interact with elastic search
 */
@SuppressWarnings("deprecation")
@Alternative
@ApplicationScoped
public class ElasticSearchRepository implements SearchRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepository.class);

    protected static final List<RestStatus> FILTER_REST_STATUS = Arrays.asList(RestStatus.CONFLICT,
            RestStatus.NOT_FOUND);

    //{"I/O reactor status: STOPPED","I/O reactor has been shut down"}
    protected static final String[] FILTER_HTTPCLIENT_MESSAGES = new String[] { "I/O reactor" };

    protected static final String[] FILTER_MESSAGES = new String[] { "type=version_conflict_engine_exception" };

    protected static final String TYPE = "type";
    protected static final String PROPERTIES = "properties";

    protected long flushInterval = 0;

    @Inject
    protected ConfigService configService;

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
                indexerQueueService.addToIndexingQueue(new QueueMessage(
                        QueueableSearchable.RequestType.BULK.toString(), bulkQueueableSearchableList));
                if (failure instanceof IllegalStateException) {
                    reconnectClientIfNeeded((IllegalStateException) failure);
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
    public void process(QueueableSearchable queueableSearchable) {
        switch (queueableSearchable.getRequestType()) {
            case INDEX:
                getBulkProcessor().add(generateIndexRequest(queueableSearchable));
                break;
            case UPDATE:
                throw new InternalLogicException("Not implemented yet");
            case DELETE:
                throw new InternalLogicException("Not implemented yet");
        }
    }

    @Override
    public void process(List<QueueableSearchable> transportSearchableList) {
        BulkRequest bulkRequest = new BulkRequest();
        for (QueueableSearchable queueableSearchable : transportSearchableList) {
            switch (queueableSearchable.getRequestType()) {
            case INDEX:
                bulkRequest.add(generateIndexRequest(queueableSearchable));
                break;
            case UPDATE:
                throw new InternalLogicException("Not implemented yet");
            case DELETE:
                throw new InternalLogicException("Not implemented yet");
            }
        }

        try {
            BulkResponse bulkResponse = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
            handleBulkResponse(-1,bulkRequest, bulkResponse);
        } catch (IOException ex) {
            LOGGER.warn("Executed bulk with complete failure, entire bulk will be retried, message: [{}]",
                    ex.getMessage());
            throw new InternalLogicException("Indexing-IOException unable to process bulk Untransportable");
        } catch (IllegalStateException ex) {
            LOGGER.warn("Executed bulk with complete failure, entire bulk will be retried, message: [{}]",
                    ex.getMessage());
            reconnectClientIfNeeded(ex);
            throw new InternalLogicException("Indexing-IOException unable to process bulk Untransportable");
        }
    }

    protected void handleBulkResponse(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
        LOGGER.info("Executed bulk [{}] with [{}] requests, hasFailures: [{}], took: [{}], ingestTook: [{}]",
                executionId, bulkRequest.numberOfActions(), bulkResponse.hasFailures(), bulkResponse.getTook(),
                bulkResponse.getIngestTook());
        if (bulkResponse.hasFailures()) {
            List<QueueableSearchable> queueableSearchableList = handleFailedBulkProcess(bulkRequest, bulkResponse);
            for (QueueableSearchable queueableSearchable: queueableSearchableList) {
                indexerQueueService.addToIndexingQueue(new QueueMessage(
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
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(parameters.getMaxResults());
        builder.query(QueryBuilders.matchAllQuery());

        if (parameters.getTimeout().isPresent()) {
            builder.timeout(new TimeValue(parameters.getTimeout().get()));
        }

        addSearchFilters(parameters.getFilters(), builder);

        if (parameters.getFields().isPresent()) {
            if (parameters.getFields().get().isEmpty()) {
                //If no fields are requested, then the result doesn't need any hits
                builder.size(0);

            } else {
                builder.fetchSource(parameters.getFields().get().toArray(new String[0]), new String[0]);
            }
        } else {
            builder.fetchSource(true);
        }


        if (!parameters.getSorting().isEmpty()) {
            Map<String, Class<?>> mapping = getFlatTypeMapping(readTypeMapping(index));
            for (Map.Entry<String, Boolean> sorting : parameters.getSorting().entrySet()) {
                // if the sorting field is of type string we need to sort on the keyword
                // property of that field
                if (mapping.get(sorting.getKey()).isAssignableFrom(String.class)) {
                    builder.sort(sorting.getKey().concat(Searchable.INDEX_SEARCH_KEYWORD),
                            sorting.getValue() ? SortOrder.ASC : SortOrder.DESC);
                } else {
                    builder.sort(sorting.getKey(), sorting.getValue() ? SortOrder.ASC : SortOrder.DESC);
                }
            }
        }

        addAggregations(parameters.getAggregations(), builder);
        searchRequest.source(builder);

        LOGGER.debug("Executing search on index {} with parameters {}, builder {}", index, parameters, builder);

        try {
            SearchResponse response = getClient().search(searchRequest, RequestOptions.DEFAULT);
            return parseSearchResponse(parameters, response);
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Failed to fetch {} entries for index {} because of {}", parameters.getMaxResults(), index,
                    e.getCause());
            return new SearchResult();
        }
    }

    protected Map<String, Object> readTypeMapping(String index) {
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
        if ("string".equals(typeName)) {
            return String.class;
        } else if ("boolean".equals(typeName)) {
            return Boolean.class;
        } else if ("date".equals(typeName)) {
            return Date.class;
        } else if ("long".equals(typeName)) {
            return Long.class;
        } else if ("double".equals(typeName)) {
            return Double.class;
        } else if ("short".equals(typeName)) {
            return Short.class;
        } else if ("byte".equals(typeName)) {
            return Byte.class;
        } else if ("integer".equals(typeName)) {
            return Integer.class;
        } else {
            return String.class;
        }
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

            if (entry.getValue() instanceof Class) {
                result.put(fieldName, (Class<?>) entry.getValue());

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

    protected void addSearchFilters(List<SearchCriteria> searchFilters, SearchSourceBuilder builder) {
        if (!searchFilters.isEmpty()) {

            if (searchFilters.size() > 1) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                for (SearchCriteria filter : searchFilters) {
                    // all filters are added with AND (means must)
                    boolQuery.must(buildInnerQuery(filter));
                }

                builder.query(boolQuery);
            } else {
                builder.query(buildInnerQuery(searchFilters.get(0)));
            }
        }
    }

    protected QueryBuilder buildInnerQuery(SearchCriteria filter) {
        if (filter instanceof RangeBasedSearchCriteria) {
            if (filter instanceof DateSearchCriteria) {
                return buildDateQuery((DateSearchCriteria) filter);
            }
            return buildRangeRangeBasedQuery((RangeBasedSearchCriteria)filter);
        }

        if (filter instanceof ExplicitSearchCriteria) {
            return buildExplicitSearchQuery((ExplicitSearchCriteria) filter);
        }

        if (filter instanceof ContainsSearchCriteria) {
            return buildContainsSearchQuery((ContainsSearchCriteria) filter);
        }

        if (filter instanceof ExistingFieldSearchCriteria) {
            return buildExistingFieldQuery((ExistingFieldSearchCriteria) filter);
        }

        if (filter instanceof CombinedSearchCriteria){
            return buildCombinedQuery((CombinedSearchCriteria) filter);
        }

        throw  new InternalLogicException("Criteria not supported " + filter.getClass().getName());
    }

    protected QueryBuilder buildDateQuery(DateSearchCriteria criteria) {
        RangeQueryBuilder rangeQuery = buildBasicRangeQuery(criteria);
        if (criteria.getTimeZone() != null) {
            rangeQuery.timeZone(criteria.getTimeZone());
        }
        return searchQueryNot(criteria, rangeQuery);
    }

    protected QueryBuilder buildRangeRangeBasedQuery(RangeBasedSearchCriteria criteria) {
        return searchQueryNot(criteria, buildBasicRangeQuery(criteria));
    }

    protected RangeQueryBuilder buildBasicRangeQuery(RangeBasedSearchCriteria criteria) {
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(criteria.getFieldName());

        if (criteria.getFrom() != null) {
            rangeQuery.from(criteria.getFrom());
            rangeQuery.includeLower(criteria.isIncludeFrom());
        }

        if (criteria.getTo() != null) {
            rangeQuery.to(criteria.getTo());
            rangeQuery.includeUpper(criteria.isIncludeTo());
        }
        return rangeQuery;
    }

    protected QueryBuilder buildExplicitSearchQuery(ExplicitSearchCriteria criteria) {

        if (criteria.getFieldValue() instanceof String) {
            String fieldValue = criteria.getFieldValue().toString();

            if (fieldValue.contains("*") || fieldValue.contains("?")) {
                // wildcard
                QueryBuilder queryBuilder = QueryBuilders.wildcardQuery(criteria.getFieldName(), fieldValue);
                return searchQueryNot(criteria, queryBuilder);
            }
        }

        MatchQueryBuilder termQuery = QueryBuilders.matchQuery(criteria.getFieldName(), criteria.getFieldValue());
        return searchQueryNot(criteria, termQuery);
    }

    protected QueryBuilder buildContainsSearchQuery(ContainsSearchCriteria criteria) {
        TermsQueryBuilder termQuery = QueryBuilders.termsQuery(criteria.getFieldName(), criteria.getFieldValues());
        return searchQueryNot(criteria, termQuery);
    }

    protected QueryBuilder buildExistingFieldQuery(ExistingFieldSearchCriteria criteria) {
        ExistsQueryBuilder existsQuery = QueryBuilders.existsQuery(criteria.getFieldName());

        if (criteria.getExists() ^ criteria.isNot()) {
            return existsQuery;
        } else {
            return QueryBuilders.boolQuery().mustNot(existsQuery);
        }
    }

    protected QueryBuilder buildCombinedQuery(CombinedSearchCriteria criteria) {
        BoolQueryBuilder innerQuery = QueryBuilders.boolQuery();
        for (SearchCriteria searchCriteria : criteria.getSearchCriteriaList()) {
            if (criteria.isAnd()) {
                innerQuery.must(buildInnerQuery(searchCriteria));
            } else {
                innerQuery.should(buildInnerQuery(searchCriteria));
            }
        }
        return innerQuery;
    }

    protected QueryBuilder searchQueryNot(FieldSearchCriteria fieldSearchCriteria, QueryBuilder query) {
        if (fieldSearchCriteria.isNot()) {
            return QueryBuilders.boolQuery().mustNot(query);
        } else {
            return query;
        }
    }

    protected void addAggregations(List<SearchAggregation> aggregations, SearchSourceBuilder builder) {
        for (SearchAggregation aggregation : aggregations) {
            if (aggregation instanceof ComplexFieldAggregation) {
                parseComplexFieldAggregation((ComplexFieldAggregation) aggregation, builder);
            } else if (aggregation instanceof SimpleFieldAggregation) {
                SimpleFieldAggregation simpleFieldAgg = (SimpleFieldAggregation) aggregation;

                builder.aggregation(getAggregationBuilder(
                        simpleFieldAgg.getAggregationType(),
                        simpleFieldAgg.getName()).field(simpleFieldAgg.getFieldName()));
            }
        }
    }
    protected void parseComplexFieldAggregation(ComplexFieldAggregation aggregation, SearchSourceBuilder builder) {
        TermsAggregationBuilder rootAggregation = null;
        TermsAggregationBuilder currentAggregation = null;

        if (aggregation.getGroupFields().isEmpty()) {
            LOGGER.warn("An aggregation with name {} was added, but it did not contain any fields to aggregate on, so it will be ignored", aggregation.getName());
            return;
        }

        for (String fieldName : aggregation.getGroupFields()) {

            if (rootAggregation == null) {
                rootAggregation = AggregationBuilders.terms(aggregation.getName()).field(fieldName)
                        .size(aggregation.getSize());
                currentAggregation = rootAggregation;
            } else {
                TermsAggregationBuilder newAggregation = AggregationBuilders.terms(fieldName)
                        .field(fieldName);
                currentAggregation.subAggregation(newAggregation);
                currentAggregation = newAggregation;
            }
        }

        if (currentAggregation != null) {
            currentAggregation
                    .subAggregation(getAggregationBuilder(aggregation.getAggregationType(), "lastAgg")
                            .field(aggregation.getFieldName()));
        }
        builder.size(aggregation.getSize());
        builder.aggregation(rootAggregation);
    }

    protected ValuesSourceAggregationBuilder getAggregationBuilder(SearchAggregation.AggregationType aggregationType,
            String aggregationName) {
        switch (aggregationType) {
        case MAX:
            return AggregationBuilders.max(aggregationName);
        case AVG:
            return AggregationBuilders.avg(aggregationName);
        case MIN:
            return AggregationBuilders.min(aggregationName);
        case SUM:
            return AggregationBuilders.sum(aggregationName);
        case CARDINALITY:
            return AggregationBuilders.cardinality(aggregationName);
        default:
            return AggregationBuilders.count(aggregationName);
        }
    }

    protected SearchResult parseSearchResponse(SearchQuery parameters, SearchResponse response) {
        return new SearchResult(
                response.getHits().getTotalHits().value,
                response.getHits().getMaxScore(),
                response.getTook().getMillis(),
                response.isTerminatedEarly() != null ? response.isTerminatedEarly() : false,
                response.isTimedOut(),
                response.getScrollId(),
                parseHits(response.getHits(), parameters.getOnlySource()),
                parseAggregations(response.getAggregations(), parameters.getAggregations()),
                TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO.equals(response.getHits().getTotalHits().relation));
    }

    protected List<JsonNode> parseHits(SearchHits searchHits, boolean onlySource) {
        List<JsonNode> hitList = new ArrayList<>();

        for (SearchHit hit : searchHits.getHits()) {
            if (onlySource) {
                hitList.add(JsonUtil.fromJson(hit.toString()).get("_source"));
            } else {
                hitList.add(JsonUtil.fromJson(hit.toString()));
            }

        }

        return hitList;
    }

    protected Map<String, AggregationResult> parseAggregations(Aggregations aggregations, List<SearchAggregation> list) {
        Map<String, AggregationResult> aggregationResults = new HashMap<>();
        Map<String, List<String>> aggregationColumnNames = new HashMap<>();

        if (list != null) {
            for (SearchAggregation searchAgg : list) {
                if (searchAgg instanceof ComplexFieldAggregation) {
                    aggregationColumnNames.put(searchAgg.getName(),
                            ((ComplexFieldAggregation) searchAgg).getGroupFields());
                }
            }
        }

        if (aggregations != null) {
            for (Aggregation agg : aggregations.asList()) {
                String aggName = agg.getName();

                if (agg instanceof InternalValueCount) {

                    aggregationResults.put(aggName, new SimpleAggregationResult(aggName, ((InternalValueCount) agg).getValue()));
                } else if (agg instanceof NumericMetricsAggregation.SingleValue) {

                    aggregationResults.put(aggName, new SimpleAggregationResult(aggName, ((NumericMetricsAggregation.SingleValue) agg).value()));
                } else if (agg instanceof StringTerms) {

                    parseStringAggregation(aggregationResults, aggregationColumnNames, agg, ((StringTerms) agg).getBuckets());
                } else if (agg instanceof ParsedStringTerms) {

                    parseStringAggregation(aggregationResults, aggregationColumnNames, agg, ((ParsedStringTerms) agg).getBuckets());
                }
            }
        }
        return aggregationResults;
    }

    protected void parseStringAggregation(Map<String, AggregationResult> aggregationResults,
            Map<String, List<String>> aggregationColumnNames, Aggregation agg, List<? extends Terms.Bucket> buckets) {
        for (Terms.Bucket bucket : buckets) {
            // first column name
            String firstColumnName = aggregationColumnNames.get(agg.getName()).get(0);
            // first column value
            String firstValue = bucket.getKeyAsString();

            Map<String, Object> columnValues = new LinkedHashMap<>();
            columnValues.put(firstColumnName, firstValue);

            analyzeInnerAggregation(aggregationResults, columnValues, agg.getName(), bucket);

        }
    }

    protected void analyzeInnerAggregation(Map<String, AggregationResult> aggregationResults,
            Map<String, Object> columnValues, String aggName, Terms.Bucket bucket) {

        // there should only ever be one inner aggregation
        if (bucket.getAggregations().asList().size() == 1) {
            Aggregation innerAggregation = bucket.getAggregations().asList().get(0);
            String secondColumnName = innerAggregation.getName();

            if (innerAggregation instanceof Terms) {
                for (Terms.Bucket innerBucket : ((Terms) innerAggregation).getBuckets()) {
                    String secondValue = innerBucket.getKeyAsString();
                    columnValues.put(secondColumnName, secondValue);

                    analyzeInnerAggregation(aggregationResults, columnValues, aggName, innerBucket);
                }
            } else {
                Object value = getValueOfAggregation(innerAggregation);
                if (aggregationResults.containsKey(aggName)) {
                    ComplexAggregationResult result = (ComplexAggregationResult) aggregationResults.get(aggName);
                    result.addValue(new AggregationResultValue(columnValues, value));
                } else {
                    ComplexAggregationResult result = new ComplexAggregationResult(aggName);
                    result.addValue(new AggregationResultValue(columnValues, value));
                    aggregationResults.put(aggName, result);
                }
            }
        }
    }

    public Object getValueOfAggregation(Aggregation innerAggregation) {
        if (innerAggregation instanceof InternalSum) {
            return String.valueOf(((InternalSum) innerAggregation).getValue());
        } else if (innerAggregation instanceof InternalValueCount) {
            return ((InternalValueCount) innerAggregation).getValue();
        } else if (innerAggregation instanceof ParsedAvg) {
            return ((ParsedAvg) innerAggregation).getValue();
        } else if (innerAggregation instanceof ParsedValueCount) {
            return ((ParsedValueCount) innerAggregation).getValue();
        } else if (innerAggregation instanceof ParsedSum) {
            return ((ParsedSum) innerAggregation).getValue();
        } else if (innerAggregation instanceof ParsedMin) {
            return ((ParsedMin) innerAggregation).getValue();
        } else if (innerAggregation instanceof ParsedMax) {
            return ((ParsedMax) innerAggregation).getValue();
        } else if (innerAggregation instanceof ParsedCardinality) {
            return ((ParsedCardinality) innerAggregation).getValue();
        }
        return null;
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
        try {
            ObjectNode objectNode = searchable.getJsonNode();
            objectNode.put(Searchable.BUSINESS_ID, searchable.getBusinessId());
            objectNode.put(Searchable.TYPE, searchable.getClassName());
            return objectNode;
        } catch (IllegalArgumentException e) {
            throw new InternalLogicException("Error while parsing searchable to json: " + searchable.getClassName() + ":" + searchable.getBusinessId(), e);
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
        }
        bulkProcessor = null;
    }

    protected BulkProcessor.Builder createBulkRequestBuilder(BulkProcessor.Listener listener) {
        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) ->
                        getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener);

        configService.get(ElasticSearchConnectionRepositoryImpl.ELASTIC_CONFIG);

        flushInterval = configService.get("FlushInterval").asInt().orElse(10);
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


    protected IndexRequest generateIndexRequest(QueueableSearchable queueableSearchable) {
        IndexRequest request = new IndexRequest(queueableSearchable.getIndex()).id(queueableSearchable.getId())
                .routing(queueableSearchable.getRouting())
                .source(queueableSearchable.getJsonSource(), XContentType.JSON);
        if (queueableSearchable.getVersion() != null && StringUtils.isNotEmpty(queueableSearchable.getId())) {
            request.versionType(VersionType.EXTERNAL_GTE).version(queueableSearchable.getVersion());
        }

        return request;
    }

    /**
     * Checks the message of the exception if its an {@link ElasticsearchException} otherwise it will be retried
     */
    public boolean exceptionIsToBeRetried(Exception e) {
        if (e instanceof ElasticsearchException) {
            ElasticsearchException elasticEx = (ElasticsearchException) e;
            if (FILTER_REST_STATUS.contains(elasticEx.status()) ||
                    Arrays.stream(FILTER_MESSAGES).anyMatch(elasticEx.getDetailedMessage()::contains)) {
                return false;
            }
        }
        return true;
    }

    protected QueueableSearchable generateQueueableSearchable(DocWriteRequest<?> request) {
        if (request instanceof UpdateRequest) {
            return generateQueueableSearchable((UpdateRequest) request);
        } else if (request instanceof IndexRequest) {
            return generateQueueableSearchable((IndexRequest) request);
        } else if (request instanceof DeleteRequest) {
            return generateQueueableSearchable((DeleteRequest) request);
        }
        throw new IllegalArgumentException("Unknown request type: " + request.getClass().getName());
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
