package com.neo.util.framework.database.impl;

import com.neo.util.framework.database.api.PersistenceContextService;
import com.neo.util.framework.database.impl.connection.TransactionalConnectionProvider;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;

@RequestScoped
public class M2PersistenceContextService implements PersistenceContextService {

    @Inject
    protected BeanManager beanManager;

    protected EntityManagerFactory emf;

    protected EntityManager em;

    @PostConstruct
    public void init() {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.bean.manager", beanManager);
        props.put(Environment.CONNECTION_PROVIDER, TransactionalConnectionProvider.class);
        emf = Persistence.createEntityManagerFactory("testPersistence", props);
        em = emf.createEntityManager();

    }

    @Override
    public EntityManager getEm() {
        return em;
    }
}
