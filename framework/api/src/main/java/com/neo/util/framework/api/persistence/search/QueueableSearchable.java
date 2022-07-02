package com.neo.util.framework.api.persistence.search;

import com.neo.util.common.impl.json.JsonUtil;

import java.io.Serializable;
import java.time.Instant;

/**
 * Generic bean type object to put and retrieve index specific messages from the Queue.
 *
 * Bean contains all data needed to retry sending index, update, delete requests.
 *
 */
public class QueueableSearchable implements Serializable {

    /**
     * Type of request reference.
     *
     * Only these types are supported at the moment.
     */
    public enum RequestType {
        INDEX, UPDATE, DELETE, BULK
    }

    /**
     * The creation date of the initial searchable
     */
    protected Instant creationDate;
    /**
     * The index where this object shall be persisted to
     */
    protected String index;
    /**
     * The id of the document
     */
    protected String id;
    /**
     *  The number of changes to the document
     */
    protected Long version;
    /**
     * The particular shard the document is being routed to
     */
    protected String routing;
    /**
     * The initial source to be persisted
     */
    protected String jsonSource;
    /**
     * The source of an update
     */
    protected String jsonUpsertSource;
    /**
     * The Request type
     */
    protected RequestType requestType;

    public QueueableSearchable(String index, String id, Long version, RequestType requestType) {
        this(index, id, version, null, null, requestType);
    }

    public QueueableSearchable(String index, String id, Long version, String routing, String jsonSource,
            RequestType requestType) {
        this(index, id, version, routing, jsonSource, null, requestType);
    }

    public QueueableSearchable(String index, String id, Long version, String routing, String jsonSource,
            String jsonUpsertSource, RequestType requestType) {
        super();
        this.creationDate = Instant.now();
        this.index = index;
        this.id = id;
        this.version = version;
        this.routing = routing;
        this.jsonSource = jsonSource;
        this.jsonUpsertSource = jsonUpsertSource;
        this.requestType = requestType;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getRouting() {
        return routing;
    }

    public String getJsonSource() {
        return jsonSource;
    }

    public void setJsonSource(String jsonSource) {
        this.jsonSource = jsonSource;
    }

    public void setJsonUpsertSource(String jsonUpsertSource) {
        this.jsonUpsertSource = jsonUpsertSource;
    }

    public String getJsonUpsertSource() {
        return jsonUpsertSource;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
