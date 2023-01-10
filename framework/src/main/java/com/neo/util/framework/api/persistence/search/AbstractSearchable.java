package com.neo.util.framework.api.persistence.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public abstract class AbstractSearchable implements Searchable {

    @JsonIgnore
    private Date creationDate = new Date();

    @JsonIgnore
    private String businessId = null;

    @JsonIgnore
    private Long version = null;

    @Override
    public String getBusinessId() {
        return businessId;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
