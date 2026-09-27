package com.neo.util.jakarta.database.repository;

import com.neo.util.api.persistence.entity.PersistenceContextProvider;
import com.neo.util.jakarta.database.entity.AddressEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AddressRepository extends AbstractDatabaseRepository<Long, AddressEntity> {

    @Inject
    public AddressRepository(PersistenceContextProvider pcp) {
        super(pcp, AddressEntity.class);
    }
}
