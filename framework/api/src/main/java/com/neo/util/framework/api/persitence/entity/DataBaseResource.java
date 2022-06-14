package com.neo.util.framework.api.persitence.entity;

import java.util.UUID;

public interface DataBaseResource extends DataBaseEntity {

    UUID getResourceOwner();
}
