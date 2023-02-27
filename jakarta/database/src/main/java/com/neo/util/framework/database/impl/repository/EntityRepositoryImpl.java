package com.neo.util.framework.database.impl.repository;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.api.repository.EntityRepository;
import com.neo.util.framework.database.impl.AbstractDatabaseRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.util.*;

@Transactional
public abstract class EntityRepositoryImpl<T extends PersistenceEntity> extends AbstractDatabaseRepository implements
        EntityRepository<T> {

    protected final Class<T> clazz;

    protected EntityRepositoryImpl(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Optional<T> fetch(Object primaryKey) {
        return super.find(primaryKey, clazz);
    }

    @Override
    public void create(T entity) {
        super.createWithAudit(entity);
    }

    @Override
    public void create(Collection<T> entities) {
        entities.forEach(super::createWithAudit);
    }

    @Override
    public void edit(T entity) {
        super.editWithAudit(entity);
    }

    @Override
    public void edit(Collection<T> entities) {
        entities.forEach(super::editWithAudit);
    }

    @Override
    public void remove(T entity) {
        super.removeWithAudit(entity);
    }

    @Override
    public void remove(Collection<T> entities) {
        entities.forEach(super::removeWithAudit);
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
