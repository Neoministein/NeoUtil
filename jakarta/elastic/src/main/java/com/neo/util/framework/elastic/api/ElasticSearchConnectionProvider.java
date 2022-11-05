package com.neo.util.framework.elastic.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.neo.util.framework.elastic.impl.ElasticSearchConnectionStatusEvent;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Serializable;

@SuppressWarnings("deprecation")
public interface ElasticSearchConnectionProvider extends Serializable {

    /**
     * Reloads the config and reconnects to elasticsearch
     */
    void reloadConfig();

    /**
     * Returns a connected client. If there isn't one yet one is created.
     */
    RestHighLevelClient getClient();

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
