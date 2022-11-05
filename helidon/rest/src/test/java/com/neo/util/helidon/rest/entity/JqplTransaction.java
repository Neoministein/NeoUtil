package com.neo.util.helidon.rest.entity;

import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityProvider;
import com.neo.util.framework.database.api.PersistenceContextService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@ApplicationScoped
public class JqplTransaction {

    @Inject
    PersistenceContextService contextService;

    @Inject EntityProvider entityRepository;


    public void save(TestPersonEntity testPersonEntity) {
        contextService.getEm().persist(testPersonEntity);
    }

    public List<TestPersonEntity> findAll() {
        return entityRepository.fetch(new EntityQuery<>(TestPersonEntity.class)).getHits();
    }

    public List<TestPersonEntity> findAllJPQL() {
        Query query = contextService.getEm().createQuery("SELECT e FROM TestPersonEntity e JOIN FETCH e.testLimbEntities l", TestPersonEntity.class);

        return query.getResultList();
    }


}
