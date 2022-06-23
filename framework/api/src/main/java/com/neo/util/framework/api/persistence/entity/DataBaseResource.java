package com.neo.util.framework.api.persistence.entity;

import java.util.UUID;

public interface DataBaseResource extends DataBaseEntity {

    UUID getResourceOwner();
}
