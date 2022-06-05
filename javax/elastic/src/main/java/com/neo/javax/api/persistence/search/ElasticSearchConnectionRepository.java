package com.neo.javax.api.persistence.search;

import com.neo.javax.api.event.ElasticSearchConnectionStatusEvent;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Serializable;

@SuppressWarnings("deprecation")
public interface ElasticSearchConnectionRepository extends Serializable {

    /**
     * Reloads the config and reconnects to elasticsearch
     */
    void reloadConfig();

    /**
     * Returns a connected client. If there isn't one yer one is created.
     */
    RestHighLevelClient getClient();

    /**
     * Check if elasticsearch is enabled
     * @return
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
