package com.neo.util.framework.database.impl;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.api.PersistenceContextProvider;
import com.neo.util.framework.database.api.EntityRepository;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Transactional
public abstract class AbstractDatabaseRepository<T extends PersistenceEntity> implements EntityRepository<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseRepository.class);

    @Inject
    protected PersistenceContextProvider pcs;

    protected final Class<T> clazz;

    protected AbstractDatabaseRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Optional<T> fetch(Object primaryKey) {
        try {
            LOGGER.trace("Searching for entity {}:{}", clazz.getSimpleName(), primaryKey);
            return Optional.ofNullable(pcs.getEm().find(clazz, primaryKey));
        } catch (NoResultException | IllegalArgumentException  ex) {
            LOGGER.trace("Unable to find entity {}:{}", clazz.getSimpleName(), primaryKey);
            return Optional.empty();
        }
    }

    @Override
    public void create(T entity) {
        pcs.getEm().persist(entity);
        LOGGER.debug("Created entity {}:{}", entity.getClass().getSimpleName(),  entity);
    }

    @Override
    public void create(Collection<T> entities) {
        entities.forEach(this::create);
    }

    @Override
    public void edit(T entity) {
        pcs.getEm().merge(entity);
        LOGGER.debug("Edited entity {}:{}",entity.getClass().getSimpleName(), entity);
    }

    @Override
    public void edit(Collection<T> entities) {
        entities.forEach(this::edit);
    }

    @Override
    public void remove(T entity) {
        pcs.getEm().remove(pcs.getEm().merge(entity));
        LOGGER.debug("Removed entity {}:{}",entity.getClass().getSimpleName(), entity);
    }

    @Override
    public void remove(Collection<T> entities) {
        entities.forEach(this::remove);
    }

    @Override
    public List<T> fetchAll(String... columnOrder) {
        CriteriaBuilder cb = pcs.getEm().getCriteriaBuilder();
        CriteriaQuery<T> cQuery = cb.createQuery(clazz);

        Root<T> root = cQuery.from(clazz);
        cQuery.select(root);
        List<Order> orderByList = new ArrayList<>();
        for (String columnToOrder : columnOrder) {
            orderByList.add(cb.asc(root.get(columnToOrder)));
        }
        cQuery.orderBy(orderByList);
        return pcs.getEm().createQuery(cQuery).getResultList();
    }

    @Override
    public Optional<T> fetchByValue(String column, String value) {
        CriteriaBuilder cb = pcs.getEm().getCriteriaBuilder();
        CriteriaQuery<T> cQuery = cb.createQuery(clazz);
        Root<T> root = cQuery.from(clazz);
        cQuery.select(root).where(cb.equal(root.get(column), value));

        try {
            return Optional.of(pcs.getEm().createQuery(cQuery).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> fetchAllByValue(String column, String value, String... columnOrder) {
        CriteriaBuilder cb = pcs.getEm().getCriteriaBuilder();
        CriteriaQuery<T> cQuery = cb.createQuery(clazz);
        Root<T> root = cQuery.from(clazz);
        cQuery.select(root).where(cb.equal(root.get(column), value));
        List<Order> orderByList = new ArrayList<>();
        for (String columnToOrder : columnOrder) {
            orderByList.add(cb.asc(root.get(columnToOrder)));
        }
        cQuery.orderBy(orderByList);
        return pcs.getEm().createQuery(cQuery).getResultList();
    }

    @Override
    public long count() {
        CriteriaQuery<Long> cQuery = pcs.getEm().getCriteriaBuilder().createQuery(Long.class);
        Root<T> root = cQuery.from(clazz);
        cQuery.select(pcs.getEm().getCriteriaBuilder().count(root));
        return pcs.getEm().createQuery(cQuery).getSingleResult();
    }
}
