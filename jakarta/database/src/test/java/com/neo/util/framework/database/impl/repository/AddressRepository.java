package com.neo.util.framework.database.impl.repository;

import com.neo.util.framework.database.impl.AbstractEntityRepository;
import com.neo.util.framework.database.impl.entity.AddressEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AddressRepository extends AbstractEntityRepository<AddressEntity> {

    public AddressRepository() {
        super(AddressEntity.class);
    }
}
