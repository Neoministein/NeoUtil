package com.neo.util.framework.database.impl.repository;

import com.neo.util.framework.database.impl.entity.AddressEntity;

public class AddressRepository extends EntityRepositoryImpl<AddressEntity> {

    public AddressRepository() {
        super(AddressEntity.class);
    }
}
