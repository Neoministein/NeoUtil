package com.neo.util.framework.elastic.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.elastic.api.IndexNamingService;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.util.TypeLiteral;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.netty4.Netty4Transport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CompletionStage;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 0)
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public abstract class AbstractElasticIntegrationTest extends ESIntegTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElasticIntegrationTest.class);

    protected static final String F_ID = "_id";
    protected static final int TIME_TO_SLEEP_IN_MILLISECOND = 500;
    protected static final int SLEEP_RETRY_COUNT = 10;

    protected static ElasticSearchRepository elasticSearchRepository;

    protected static RestClient restClient;

    protected TestConfigService configService = new TestConfigService(Map.of(
            ElasticSearchConnectionRepositoryImpl.ENABLED_CONFIG, true,
            ElasticSearchRepository.FLUSH_INTERVAL_CONFIG, 1
    ));

    protected static ElasticSearchConnectionRepositoryImpl connection = new ElasticSearchConnectionRepositoryImpl();
    protected IndexNamingService indexNamingService = createIndexNameService(configService);

    IndexNamingService createIndexNameService(ConfigService cfg) {
        IndexNamingServiceImpl indexNamingService = new IndexNamingServiceImpl() {
            {
                configService = cfg;
                indexNamePrefixes = new HashMap<>();
                indexPeriods = new HashMap<>();
            }
        };
        indexNamingService.postConstruct();
        return indexNamingService;
    }

    @Override
    protected boolean ignoreExternalCluster() {
        return true;
    }

    @Override
    protected boolean addMockTransportService() {
        return false;
    }

    @Override
    protected boolean addMockHttpTransport() {
        return false;
    }

    /**
     * Randomize netty settings
     *
     * <a href="https://stackoverflow.com/questions/47766777/what-is-the-purpose-of-this-propertyes-set-netty-runtime-available-processors">This is why</a>
     */
    @Override
    protected Settings nodeSettings(int nodeOrdinal, Settings settings) {
        Settings.Builder builder = Settings.builder().put(super.nodeSettings(nodeOrdinal, settings));
        // randomize netty settings
        if (randomBoolean()) {
            builder.put(Netty4Transport.WORKER_COUNT.getKey(), random().nextInt(3) + 1);
        }
        builder.put(NetworkModule.TRANSPORT_TYPE_KEY, Netty4Plugin.NETTY_TRANSPORT_NAME);
        builder.put(NetworkModule.HTTP_TYPE_KEY, Netty4Plugin.NETTY_HTTP_TRANSPORT_NAME);
        return builder.build();
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return List.of(Netty4Plugin.class);
    }

    /**
     * Look at {@link #nodeSettings(int nodeOrdinal,Settings settings) NodeSettings}
     */
    @BeforeClass
    public static void enableMultipleNettyProcessors() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        internalCluster().startNodes(1);
        ensureStableCluster(1);
        restClient = getRestClient();
        LOGGER.info("Elasticsearch node started at [{}]", restClient.getNodes().get(0).getHost().toString());
        configService.addConfig(ElasticSearchConnectionRepositoryImpl.NODE_CONFIG, List.of(restClient.getNodes().get(0).getHost().toString()));
        connection.configService = configService;
        connection.connectionStatusEvent = new EventMock<>();

        initialiseElasticSearchProvider();
    }

    @After
    public void afterTest() {
        connection.disconnect();
    }

    @AfterClass
    public static void disconnectElasticsearchConnector() {
        connection.disconnect();
    }

    protected void initialiseElasticSearchProvider() {
        connection.postConstruct();
        connection.connect();
        elasticSearchRepository = new ElasticSearchRepository();
        elasticSearchRepository.configService = configService;
        elasticSearchRepository.connection = connection;
        elasticSearchRepository.indexerQueueService = new PretendIndexerNotificationService();
        elasticSearchRepository.indexNameService = indexNamingService;
        elasticSearchRepository.setupBulkProcessor();
    }

    /*
        Helper methods
     */

    protected boolean closeIndex(String indexName) throws IOException {
        CloseIndexRequest request = new CloseIndexRequest(indexName);
        AcknowledgedResponse indexResponse = elasticSearchRepository.getClient().indices().close(request,
                RequestOptions.DEFAULT);
        return indexResponse.isAcknowledged();
    }

    protected boolean openIndex(String indexName) throws IOException {
        OpenIndexRequest request = new OpenIndexRequest(indexName);
        AcknowledgedResponse indexResponse = elasticSearchRepository.getClient().indices().open(request,
                RequestOptions.DEFAULT);
        return indexResponse.isAcknowledged();
    }

    protected SearchResponse<ObjectNode> fetchDocumentsInIndex(String uuid, String indexName) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(indexName);
        if (StringUtils.isEmpty(uuid)) {
            builder.query(QueryBuilders.matchAll().build()._toQuery());
        } else {
            builder.query(QueryBuilders.term(q -> q.field(F_ID).value(uuid)));
        }
        SearchRequest searchRequest = builder.build();
        LOGGER.info("SearchRequest: [{}]", searchRequest);

        try {
            SearchResponse<ObjectNode> searchResponse = connection.getApiClient().search(searchRequest, ObjectNode.class);
            LOGGER.info("searchResponse: [{}]", searchResponse.toString());
            return searchResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected SearchResponse<ObjectNode> fetchAllDocumentsOnIndex(String indexName) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAll().build()._toQuery());
        SearchRequest searchRequest = builder.build();
        LOGGER.info("SearchRequest: [{}]", searchRequest);
        try {
            SearchResponse<ObjectNode> searchResponse = connection.getApiClient().search(searchRequest, ObjectNode.class);
            LOGGER.info("searchResponse: [{}]", searchResponse.toString());
            return searchResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void validateDocumentInIndex(String uuid, String indexName, boolean mustExist) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(indexName);
        builder.query(co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.term(q -> q.field(F_ID).value(uuid)));
        co.elastic.clients.elasticsearch.core.SearchRequest searchQuery = builder.build();
        LOGGER.info("SearchRequest: [{}]", builder);

        IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
            flushAndRefresh();

            try {
                SearchResponse<ObjectNode> searchResponse = connection.getApiClient().search(searchQuery, ObjectNode.class);
                LOGGER.info("SearchResponse: [{}]", searchResponse.toString());
                if (mustExist) {
                    if (searchResponse.hits().total() == null || searchResponse.hits().total().value() != 1) {
                        return false;
                    }
                    return uuid.equals(searchResponse.hits().hits().get(0).id());
                } else {
                    return searchResponse.hits().total() != null && searchResponse.hits().total().value() == 0;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    protected void validateDocumentInIndex(String uuid, String indexName, String fieldName, String fieldValue) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(indexName);
        builder.query(QueryBuilders.term(q -> q.field(F_ID).value(uuid)));
        SearchRequest searchQuery = builder.build();
        LOGGER.info("SearchRequest: [{}]", searchQuery);

        IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
            flushAndRefresh();

            try {
                SearchResponse<ObjectNode> searchResponse = connection.getApiClient().search(searchQuery, ObjectNode.class);
                LOGGER.info("SearchResponse: [{}]", searchResponse.toString());
                if (searchResponse.hits().total() == null || searchResponse.hits().total().value() != 1) {
                    return false;
                }
                ObjectNode source = searchResponse.hits().hits().get(0).source();
                if (source == null) {
                    return false;
                }
                JsonNode field = source.get(fieldName);
                return field != null && fieldValue.equals(field.asText());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });

    }

    protected void validateDocumentInIndex(String uuid, String indexName, String fieldName, String fieldValue,
            String fieldToCheck, boolean fieldToCheckMustExist) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(indexName);
        builder.query(QueryBuilders.term(q -> q.field(F_ID).value(uuid)));
        SearchRequest searchQuery = builder.build();
        LOGGER.info("SearchRequest: [{}]", searchQuery);

        IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {

            flushAndRefresh();

            try {
                SearchResponse<ObjectNode> searchResponse = connection.getApiClient().search(searchQuery, ObjectNode.class);
                LOGGER.info("SearchResponse: [{}]", searchResponse.toString());
                if (searchResponse.hits().total() == null || searchResponse.hits().total().value() != 1) {
                    return false;
                }
                ObjectNode source = searchResponse.hits().hits().get(0).source();
                if (source == null) {
                    return false;
                }
                JsonNode field = source.get(fieldName);
                if (field == null && !fieldValue.equals(field.asText())) {
                    return false;
                }

                if (fieldToCheckMustExist) {
                    return source.get(fieldToCheck) != null;
                }
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    protected static class EventMock<T> implements Event<T> {
        private Object event;

        public Object getEvent() {
            return event;
        }

        @Override
        public void fire(Object event) {
            this.event = event;
        }

        @Override
        public Event<T> select(Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends T> Event<U> select(Class<U> clazz, Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends T> Event<U> select(TypeLiteral<U> u, Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends T> CompletionStage<U> fireAsync(U u) {
            return null;
        }

        @Override
        public <U extends T> CompletionStage<U> fireAsync(U u, NotificationOptions options) {
            return null;
        }
    }
}
