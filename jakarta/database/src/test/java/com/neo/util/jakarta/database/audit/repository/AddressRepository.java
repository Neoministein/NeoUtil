package com.neo.util.jakarta.database.audit.repository;

import com.neo.util.jakarta.database.PersistenceContextProvider;
import com.neo.util.jakarta.database.audit.entity.AddressEntity;
import com.neo.util.jakarta.database.repository.AbstractDatabaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AddressRepository extends AbstractDatabaseRepository<Long, AddressEntity> {

    @Inject
    public AddressRepository(PersistenceContextProvider pcp) {
        super(pcp, AddressEntity.class);
    }
}
