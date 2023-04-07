package com.neo.util.framework.api.persistence.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public abstract class AbstractSearchable implements Searchable {

    @JsonIgnore
    private Instant creationDate = Instant.now();

    @JsonIgnore
    private String businessId = null;

    @JsonIgnore
    private Long version = null;

    @Override
    public String getBusinessId() {
        return businessId;
    }

    @Override
    public Instant getCreationDate() {
        return creationDate;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
