package com.neo.javax.api.persitence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.common.impl.json.JsonUtil;
import com.neo.javax.api.persitence.IndexPeriod;

import java.util.Date;

public class GenericSearchable implements Searchable {

    @JsonIgnore
    private ObjectNode jsonNode = JsonUtil.emptyObjectNode();

    @JsonIgnore
    private Date creationDate = new Date();

    @JsonIgnore
    private String indexName;

    @JsonIgnore
    private String businessId;

    @Override
    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public IndexPeriod getIndexPeriod() {
        return IndexPeriod.DEFAULT;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    protected void setJsonNode(ObjectNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    @Override
    public ObjectNode getJsonNode() {
        return jsonNode;
    }
}
