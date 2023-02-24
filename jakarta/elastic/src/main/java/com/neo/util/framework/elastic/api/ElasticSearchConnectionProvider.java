package com.neo.util.framework.elastic.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.Serializable;

public interface ElasticSearchConnectionProvider extends Serializable {

    /**
     * Reloads the config and reconnects to elasticsearch
     */
    void reloadConfig();

    /**
     *  Returns a connected ElasticSearch client. If there isn't one yet one is created.
     */
    ElasticsearchClient getApiClient();

    /**
     * Check if elasticsearch is enabled
     */
    boolean enabled();

    /**
     * Disconnects the client from elasticsearch
     */
    void disconnect();

    /**
     * Connects the client to elasticsearch and fires a {@link ElasticSearchConnectionStatusEvent}
     */
    void connect();
}
