package com.neo.util.framework.elastic.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationReadyEvent;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionRepository;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for upholding the connection to the elasticsearch nodes.
 */
@SuppressWarnings("deprecation")
@ApplicationScoped
public class ElasticSearchConnectionRepositoryImpl implements ElasticSearchConnectionRepository {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConnectionRepositoryImpl.class);

    protected static final String DEFAULT_SCHEME = "http";
    protected static final String LOCALHOST_HOST_NAME = "127.0.0.1";
    protected static final int DEFAULT_PORT = 9200;

    protected static final String DEFAULT_URL = DEFAULT_SCHEME + "://" + LOCALHOST_HOST_NAME + ":" + DEFAULT_PORT;

    protected static final String CONFIG_PREFIX = ElasticSearchRepository.CONFIG_PREFIX;
    protected static final String ENABLED_CONFIG = CONFIG_PREFIX + ".enabled";
    protected static final String NODE_CONFIG = CONFIG_PREFIX + ".nodes";
    protected static final String CREDENTIALS_CONFIG = CONFIG_PREFIX + ".credentials";

    protected boolean enabled = false;

    @Inject
    protected ConfigService configService;

    @Inject
    protected Event<ElasticSearchConnectionStatusEvent> connectionStatusEvent;

    protected volatile RestHighLevelClient client;
    protected volatile ElasticsearchClient elasticsearchClient;

    protected List<String> nodeList = new ArrayList<>();

    protected final AtomicBoolean connectorInitializationOngoing = new AtomicBoolean(false);

    @PostConstruct
    public void postConstruct() {
        reloadConfig();
    }

    public void reloadConfig() {
        LOGGER.debug("Loading elastic search configuration");
        List<String> nodes = configService.get(NODE_CONFIG).asList(String.class).orElse(List.of(DEFAULT_URL));

        nodeList = nodes;
        LOGGER.debug("Elasticsearch nodes {}", nodes);
        this.enabled = configService.get(ENABLED_CONFIG).asBoolean().orElse(false);
        if (enabled) {
            connect();
        } else {
            LOGGER.info("Elasticsearch isn't enabled. Connection won't be established");
        }
    }

    public void onStartUp(@Observes ApplicationReadyEvent preReadyEvent) {
        LOGGER.debug("Startup event received");
    }

    /**
     * Returns a connected client. If there isn't one yer one is created.
     */
    public RestHighLevelClient getClient() {
        throwIfNotConnected(client);
        return client;
    }

    public ElasticsearchClient getApiClient() {
        throwIfNotConnected(elasticsearchClient);
        return elasticsearchClient;
    }

    protected void throwIfNotConnected(Object client) {
        if (client != null) {
            return;
        }

        if (connectorInitializationOngoing.compareAndSet(false, true)) {
            try {
                connect();
                return;
            } catch (Exception e) {
                connectorInitializationOngoing.set(false);
            }
        }
        LOGGER.error("Elastic client is not yet ready yet");
        throw new IllegalStateException("Elastic client is not yet ready");
    }

    public boolean enabled() {
        return enabled;
    }

    /**
     * Disconnects the client from elasticsearch
     */
    public synchronized void disconnect() {
        if (client == null && elasticsearchClient == null) {
            return;
        }

        try {
            client.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close connection to elasticsearch correctly. Setting client to null");
        } finally {
            client = null;
            elasticsearchClient = null;
            connectorInitializationOngoing.set(false);
        }
    }

    /**
     * Connects the client to elasticsearch and fires a {@link ElasticSearchConnectionStatusEvent}
     */
    public synchronized void connect() {
        if (client != null && elasticsearchClient != null) {
            return;
        }

        try {
            List<HttpHost> nodes = getNodes();
            initializeClient(nodes);
        } finally {
            // end initialization process
            connectorInitializationOngoing.set(false);
        }
    }

    /**
     * Create Elasticsearch client based on one or more HttpHost nodes.
     *
     * @param nodes
     *            list of nodes
     */
    protected synchronized void initializeClient(List<HttpHost> nodes) {
        CredentialsProvider credentialsProvider = getCredentialsProvider();

        for (HttpHost node : nodes) {
            LOGGER.info("Elasticsearch configuration. Host:{} Port:{} Protocol: {}", node.getHostName(), node.getPort(),
                    node.getSchemeName());
        }

        RestClient restClient;

        if (credentialsProvider != null) {
            restClient = RestClient.builder(nodes.toArray(new HttpHost[nodes.size()]))
                    .setHttpClientConfigCallback(
                            httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();
        } else {
            restClient = RestClient.builder(nodes.toArray(new HttpHost[nodes.size()]))
                    .build();
        }

        client = new RestHighLevelClientBuilder(restClient).setApiCompatibilityMode(true).build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        elasticsearchClient = new ElasticsearchClient(transport);

        // hlrc and esClient share the same httpClient

        LOGGER.debug("Initializing elasticsearch complete");
        connectionStatusEvent.fire(new ElasticSearchConnectionStatusEvent(ElasticSearchConnectionStatusEvent.STATUS_EVENT_CONNECTED));
    }

    /**
     * Creates a CredentialsProvider in order to connect to the elastic search cluster if credentials are needed.
     * If no username or password is found in the {@link ConfigService} null will be returned
     *
     * @return credentialsProvider
     */
    protected CredentialsProvider getCredentialsProvider() {
        Config config = configService.get(CREDENTIALS_CONFIG);


        String username = config.get("username").asString().orElse(null);
        String password = config.get("password").asString().orElse(null);

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return null;
        }
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

    protected List<HttpHost> getNodes() {
        try {
            List<HttpHost> nodes = new ArrayList<>(nodeList.size());
            for (String nodeUrl : nodeList) {
                if (!StringUtils.isEmpty(nodeUrl)) {
                    nodes.add(HttpHost.create(nodeUrl));
                }
            }
            return nodes;
        } catch (Exception ex) {
            LOGGER.error("Failed load ElasticSearch nodes {} with exception: {}", nodeList, ex);
            throw ex;
        }
    }
}
