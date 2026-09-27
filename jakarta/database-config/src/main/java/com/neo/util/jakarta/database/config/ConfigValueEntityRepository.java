package com.neo.util.jakarta.database.config;

import com.neo.util.api.persistence.entity.PersistenceContextProvider;
import com.neo.util.jakarta.database.repository.AbstractDatabaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConfigValueEntityRepository extends AbstractDatabaseRepository<String, ConfigValueEntity> {

    @Inject
    protected ConfigValueEntityRepository(PersistenceContextProvider pcp) {
        super(pcp, ConfigValueEntity.class);
    }
}
