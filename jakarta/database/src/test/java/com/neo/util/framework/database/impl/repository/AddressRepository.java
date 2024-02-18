package com.neo.util.framework.database.impl.repository;

import com.neo.util.framework.database.api.PersistenceContextProvider;
import com.neo.util.framework.database.impl.AbstractDatabaseRepository;
import com.neo.util.framework.database.impl.entity.AddressEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AddressRepository extends AbstractDatabaseRepository<AddressEntity> {

    @Inject
    public AddressRepository(PersistenceContextProvider pcp) {
        super(pcp, AddressEntity.class);
    }
}
