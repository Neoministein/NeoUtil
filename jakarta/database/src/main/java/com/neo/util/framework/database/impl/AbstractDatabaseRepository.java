package com.neo.util.framework.database.impl;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.api.EntityRepository;
import com.neo.util.framework.database.api.PersistenceContextProvider;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Transactional
public abstract class AbstractDatabaseRepository<T extends PersistenceEntity> implements EntityRepository<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseRepository.class);

    protected final PersistenceContextProvider pcp;
    protected final Class<T> clazz;

    protected AbstractDatabaseRepository(PersistenceContextProvider pcp, Class<T> clazz) {
        this.pcp = pcp;
        this.clazz = clazz;
    }

    @Override
    public Optional<T> fetch(Object primaryKey) {
        try {
            LOGGER.trace("Searching for entity {}:{}", clazz.getSimpleName(), primaryKey);
            return Optional.ofNullable(pcp.getEm().find(clazz, primaryKey));
        } catch (NoResultException | IllegalArgumentException  ex) {
            LOGGER.trace("Unable to find entity {}:{}", clazz.getSimpleName(), primaryKey);
            return Optional.empty();
        }
    }

    @Override
    public void create(T entity) {
        pcp.getEm().persist(entity);
        LOGGER.debug("Created entity {}:{}", entity.getClass().getSimpleName(),  entity);
    }

    @Override
    public void create(Collection<T> entities) {
        entities.forEach(this::create);
    }

    @Override
    public void edit(T entity) {
        pcp.getEm().merge(entity);
        LOGGER.debug("Edited entity {}:{}",entity.getClass().getSimpleName(), entity);
    }

    @Override
    public void edit(Collection<T> entities) {
        entities.forEach(this::edit);
    }

    @Override
    public void remove(T entity) {
        pcp.getEm().remove(pcp.getEm().merge(entity));
        LOGGER.debug("Removed entity {}:{}",entity.getClass().getSimpleName(), entity);
    }

    @Override
    public void remove(Collection<T> entities) {
        entities.forEach(this::remove);
    }

    @Override
    public List<T> fetchAll(String... columnOrder) {
        CriteriaBuilder cb = pcp.getEm().getCriteriaBuilder();
        CriteriaQuery<T> cQuery = cb.createQuery(clazz);

        Root<T> root = cQuery.from(clazz);
        cQuery.select(root);
        List<Order> orderByList = new ArrayList<>();
        for (String columnToOrder : columnOrder) {
            orderByList.add(cb.asc(root.get(columnToOrder)));
        }
        cQuery.orderBy(orderByList);
        return pcp.getEm().createQuery(cQuery).getResultList();
    }

    @Override
    public Optional<T> fetchByValue(String column, String value) {
        CriteriaBuilder cb = pcp.getEm().getCriteriaBuilder();
        CriteriaQuery<T> cQuery = cb.createQuery(clazz);
        Root<T> root = cQuery.from(clazz);
        cQuery.select(root).where(cb.equal(root.get(column), value));

        try {
            return Optional.of(pcp.getEm().createQuery(cQuery).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> fetchAllByValue(String column, String value, String... columnOrder) {
        CriteriaBuilder cb = pcp.getEm().getCriteriaBuilder();
        CriteriaQuery<T> cQuery = cb.createQuery(clazz);
        Root<T> root = cQuery.from(clazz);
        cQuery.select(root).where(cb.equal(root.get(column), value));
        List<Order> orderByList = new ArrayList<>();
        for (String columnToOrder : columnOrder) {
            orderByList.add(cb.asc(root.get(columnToOrder)));
        }
        cQuery.orderBy(orderByList);
        return pcp.getEm().createQuery(cQuery).getResultList();
    }

    @Override
    public long count() {
        CriteriaQuery<Long> cQuery = pcp.getEm().getCriteriaBuilder().createQuery(Long.class);
        Root<T> root = cQuery.from(clazz);
        cQuery.select(pcp.getEm().getCriteriaBuilder().count(root));
        return pcp.getEm().createQuery(cQuery).getSingleResult();
    }
}
