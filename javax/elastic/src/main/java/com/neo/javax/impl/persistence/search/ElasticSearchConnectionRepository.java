package com.neo.javax.impl.persistence.search;

import com.neo.common.impl.StringUtils;
import com.neo.javax.api.config.Config;
import com.neo.javax.api.config.ConfigService;
import com.neo.javax.api.event.ApplicationReadyEvent;
import com.neo.javax.api.event.ElasticSearchConnectionStatusEvent;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for upholding the connection to the elasticsearch nodes.
 */
@SuppressWarnings("deprecation")
@ApplicationScoped
public class ElasticSearchConnectionRepository implements Serializable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConnectionRepository.class);

    protected static final String DEFAULT_SCHEME = "http";
    protected static final String LOCALHOST_HOST_NAME = "127.0.0.1";
    protected static final int DEFAULT_PORT = 9200;

    protected static final String DEFAULT_URL = DEFAULT_SCHEME + "://" + LOCALHOST_HOST_NAME + ":" + DEFAULT_PORT;

    protected static final String ELASTIC_CONFIG = "elastic";
    protected static final String NODE_CONFIG = "nodes";
    protected static final String CREDENTIALS_CONFIG = "credentials";
    protected static final String ENABLED_CONFIG = "enabled";

    protected boolean enabled = false;

    @Inject
    ConfigService configService;

    @Inject
    Event<ElasticSearchConnectionStatusEvent> connectionStatusEvent;

    private volatile RestHighLevelClient client;

    private List<String> nodeList = new ArrayList<>();

    private final AtomicBoolean connectorInitializationOngoing = new AtomicBoolean(false);

    @PostConstruct
    public void postConstruct() {
        reloadConfig();
    }

    public void reloadConfig() {
        LOGGER.debug("Loading elastic search configuration");
        Config elasticConfig = configService.get(ELASTIC_CONFIG);
        List<String> nodes = elasticConfig.get(NODE_CONFIG).asList(String.class).orElse(List.of(DEFAULT_URL));

        nodeList = nodes;
        LOGGER.debug("Elasticsearch nodes {}", nodes);
        this.enabled = elasticConfig.get(ENABLED_CONFIG).asBoolean().orElse(false);
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
        RestHighLevelClient c = client;

        if (c != null) {
            return c;
        }

        if (connectorInitializationOngoing.compareAndSet(false, true)) {
            try {
                connect();
                return client;
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
    protected synchronized void disconnect() {
        if (client == null) {
            return;
        }

        try {
            client.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close connection to elasticsearch correctly. Setting client to null");
        } finally {
            client = null;
            connectorInitializationOngoing.set(false);
        }
    }

    /**
     * Connects the client to elasticsearch and fires a {@link ElasticSearchConnectionStatusEvent}
     */
    protected synchronized void connect() {
        if (client != null) {
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

        if (credentialsProvider != null) {
            client = new RestHighLevelClient(RestClient.builder(nodes.toArray(new HttpHost[nodes.size()]))
                    .setHttpClientConfigCallback(
                            httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
        } else {
            client = new RestHighLevelClient(RestClient.builder(nodes.toArray(new HttpHost[nodes.size()])));
        }

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
        Config config = configService.get(ELASTIC_CONFIG).get(CREDENTIALS_CONFIG);


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
                nodes.add(HttpHost.create(nodeUrl));
            }
            return nodes;
        } catch (Exception ex) {
            LOGGER.error("Failed load ElasticSearch nodes {} with exception: {}", nodeList, ex);
            throw ex;
        }
    }
}
