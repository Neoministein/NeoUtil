package com.neo.javax.api.persitence.entity;

import java.util.UUID;

public interface DataBaseResource extends DataBaseEntity {

    UUID getResourceOwner();
}
