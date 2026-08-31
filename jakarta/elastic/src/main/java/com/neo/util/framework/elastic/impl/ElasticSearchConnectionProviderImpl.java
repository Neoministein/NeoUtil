package com.neo.util.framework.elastic.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionProvider;
import com.neo.util.framework.elastic.api.ElasticSearchConnectionStatusEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for upholding the connection to the elasticsearch nodes.
 */
@ApplicationScoped
public class ElasticSearchConnectionProviderImpl implements ElasticSearchConnectionProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConnectionProviderImpl.class);

    protected static final String DEFAULT_SCHEME = "http";
    protected static final String LOCALHOST_HOST_NAME = "127.0.0.1";
    protected static final int DEFAULT_PORT = 9200;

    protected static final String DEFAULT_URL = DEFAULT_SCHEME + "://" + LOCALHOST_HOST_NAME + ":" + DEFAULT_PORT;

    protected static final String CONFIG_PREFIX = ElasticSearchProvider.CONFIG_PREFIX;
    protected static final String ENABLED_CONFIG = CONFIG_PREFIX + ".enabled";
    protected static final String NODE_CONFIG = CONFIG_PREFIX + ".nodes";
    protected static final String CREDENTIALS_CONFIG = CONFIG_PREFIX + ".credentials";

    protected final ConfigService configService;
    protected final Event<ElasticSearchConnectionStatusEvent> connectionStatusEvent;

    protected boolean enabled = false;

    protected boolean initialized = false;

    protected volatile ElasticsearchClient elasticsearchClient;

    protected List<String> nodeList = new ArrayList<>();

    protected final AtomicBoolean connectorInitializationOngoing = new AtomicBoolean(false);

    @Inject
    public ElasticSearchConnectionProviderImpl(ConfigService configService,  Event<ElasticSearchConnectionStatusEvent> connectionStatusEvent) {
        this.configService = configService;
        this.connectionStatusEvent = connectionStatusEvent;
        reloadConfig();
        initialized = true;
    }

    public void reloadConfig() {
        LOGGER.debug("Loading elastic search configuration");

        List<String> nodes = configService.getAsList(String.class, x -> x, NODE_CONFIG).orElse(List.of(DEFAULT_URL));

        nodeList = nodes;
        LOGGER.info("Elasticsearch nodes {}", nodes);
        this.enabled = configService.getAsBoolean(ENABLED_CONFIG).orElse(false);
        if (enabled) {
            connect();
        } else {
            LOGGER.info("Elasticsearch isn't enabled. Connection won't be established");
        }
    }

    public void onStartUp(@Observes @Priority(PriorityConstants.LIBRARY_BEFORE) ApplicationPreReadyEvent preReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent processed");
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
        if (elasticsearchClient == null) {
            return;
        }

        try {
            elasticsearchClient._transport().close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close connection to elasticsearch correctly. Setting client to null");
        } finally {
            elasticsearchClient = null;
            connectorInitializationOngoing.set(false);
        }
    }

    /**
     * Connects the client to elasticsearch and fires a {@link ElasticSearchConnectionStatusEvent}
     */
    public synchronized void connect() {
        if (elasticsearchClient != null) {
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
        CredentialsProvider credentialsProvider = getCredentialsProvider(nodes);

        for (HttpHost node : nodes) {
            LOGGER.debug("Elasticsearch configuration. Protocol: [{}] Host: [{}] Port: [{}]",
                    node.getSchemeName(), node.getHostName(), node.getPort());
        }

        Rest5Client restClient;

        if (credentialsProvider != null) {
            restClient = Rest5Client.builder(nodes.toArray(new HttpHost[0]))
                    .setHttpClientConfigCallback(
                            httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();
        } else {
            restClient = Rest5Client.builder(nodes.toArray(new HttpHost[0]))
                    .build();
        }

        ElasticsearchTransport transport = new Rest5ClientTransport(restClient, new Jackson3JsonpMapper(JsonUtil.createMapper()));

        elasticsearchClient = new ElasticsearchClient(transport);

        LOGGER.debug("Initialization of Elasticsearch Connection complete");

        //On first initialization it's false and this is used to let the BulkIngester to be initialized by the ApplicationReadEvent
        if (initialized) {
            connectionStatusEvent.fire(new ElasticSearchConnectionStatusEvent(ElasticSearchConnectionStatusEvent.STATUS_EVENT_CONNECTED));
        }

    }

    /**
     * Creates a CredentialsProvider in order to connect to the elastic search cluster if credentials are needed.
     * If no username or password is found in the {@link ConfigService} null will be returned
     *
     * @return credentialsProvider
     */
    protected CredentialsProvider getCredentialsProvider(List<HttpHost> nodes) {
        String username = configService.getAsString(CREDENTIALS_CONFIG,"username").orElse(null);
        String password = configService.getAsString(CREDENTIALS_CONFIG,"password").orElse(null);

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return null;
        }
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        for (HttpHost node: nodes) {
            credentialsProvider.setCredentials(new AuthScope(node), new UsernamePasswordCredentials(username, password.toCharArray()));
        }

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
            throw new IllegalStateException(ex);
        }
    }
}
