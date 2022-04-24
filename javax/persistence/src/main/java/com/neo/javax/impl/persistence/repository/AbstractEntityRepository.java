package com.neo.javax.impl.persistence.repository;



import com.neo.common.impl.exception.InternalLogicException;
import com.neo.javax.api.persitence.criteria.*;
import com.neo.javax.api.persitence.repository.EntityRepository;
import com.neo.javax.api.persitence.entity.DataBaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;

@Transactional( rollbackOn = PersistenceException.class)
public abstract class AbstractEntityRepository<T extends DataBaseEntity> implements EntityRepository<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityRepository.class);

    protected final Class<T> entityClass;

    protected AbstractEntityRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    @Override
    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    @Override
    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    @Override
    public Optional<T> find(Object primaryKey) {
        try {
            return Optional.of(getEntityManager().find(entityClass, primaryKey));
        } catch (NoResultException | IllegalArgumentException  ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> findAll() {
        @SuppressWarnings("java:S3740")
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    @Override
    public int count() {
        @SuppressWarnings("java:S3740")
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }


    public int count(List<? extends SearchCriteria> filters) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<T> root = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(root));
        Query q = getEntityManager().createQuery(cq);
        cq.where(addSearchFilters(filters, cb, root));
        return ((Long) q.getSingleResult()).intValue();
    }

    @Override
    public Optional<T> find(List<? extends SearchCriteria> filters) {
        return Optional.ofNullable(createTypedQuery(filters, Collections.emptyMap()).getSingleResult());
    }

    @Override
    public List<T> find(List<? extends SearchCriteria> filters, Map<String, Boolean> sorting, int offset) {
        return createTypedQuery(filters, sorting).setFirstResult(offset).getResultList();
    }

    @Override
    public List<T> find(List<? extends SearchCriteria> filters, Map<String, Boolean> sorting, int offset, int maxReturn) {
        return createTypedQuery(filters, sorting).setFirstResult(offset).setMaxResults(maxReturn).getResultList();
    }

    protected TypedQuery<T> createTypedQuery(List<? extends SearchCriteria> filter, Map<String, Boolean> orders) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

        AbstractQuery<T> aQuery = cb.createQuery(entityClass);

        Root<T> root = aQuery.from(entityClass);

        aQuery.where(addSearchFilters(filter, cb, root));

        CriteriaQuery<T> cQuery = ((CriteriaQuery<T>) aQuery).select(root);
        cQuery.orderBy(mapOrders(orders, cb, root));

        return getEntityManager().createQuery(cQuery);
    }

    protected List<Order> mapOrders(Map<String, Boolean> orders, CriteriaBuilder cb, Root<T> root) {
        List<Order> orderList = new ArrayList<>();
        for (Map.Entry<String, Boolean> order: orders.entrySet()) {
            if (order.getValue().booleanValue()) {
                orderList.add(cb.asc(root.get(order.getKey())));
            } else {
                orderList.add(cb.desc(root.get(order.getKey())));
            }

        }
        return orderList;
    }

    protected Predicate addSearchFilters(List<? extends SearchCriteria> searchFilters, CriteriaBuilder cb, Root<T> root) {
        Predicate predicate = cb.isTrue(cb.literal(true));
        if (!searchFilters.isEmpty()) {
            if (searchFilters.size() > 1) {
                for (SearchCriteria filter : searchFilters) {
                    // all filters are added with AND (means must)
                    predicate = cb.and(predicate, buildInnerQuery(filter, cb, root));
                }
            } else {
                return buildInnerQuery(searchFilters.get(0), cb, root);
            }
        }
        return predicate;
    }

    protected Predicate buildInnerQuery(SearchCriteria filter, CriteriaBuilder cb, Root<T> root) {
        if (filter instanceof DoubleRangeSearchCriteria) {
            return buildRangeQuery((DoubleRangeSearchCriteria) filter, cb, root);
        }
        if (filter instanceof LongRangeSearchCriteria) {
            return buildRangeQuery((LongRangeSearchCriteria) filter, cb, root);
        }

        if (filter instanceof ExplicitSearchCriteria) {
            return buildExplicitSearchQuery((ExplicitSearchCriteria) filter, cb, root);
        }

        if (filter instanceof ContainsSearchCriteria) {
            return buildContainsSearchQuery((ContainsSearchCriteria) filter, cb, root);
        }

        if (filter instanceof ExistingFieldSearchCriteria) {
            return buildAnyNoneQuery((ExistingFieldSearchCriteria) filter, cb, root);
        }
        if (filter instanceof  CombinedSearchCriteria) {
            return buildCombinedQuery((CombinedSearchCriteria) filter, cb, root);
        }

        throw new InternalLogicException("Criteria not supported " + filter.getClass().getName());
    }

    protected Predicate buildRangeQuery(LongRangeSearchCriteria criteria, CriteriaBuilder cb, Root<T> root) {
        if (criteria.isIncludeFrom()) {
            if (criteria.isIncludeTo()) {
                return cb.between(root.get(criteria.getFieldName()), criteria.getFrom(), criteria.getTo());
            } else {
                return cb.ge(root.get(criteria.getFieldName()), criteria.getFrom());
            }
        }
        if (criteria.isIncludeTo()) {
            return cb.le(root.get(criteria.getFieldName()), criteria.getTo());
        }
        return cb.isTrue(cb.literal(true));
    }

    protected Predicate buildRangeQuery(DoubleRangeSearchCriteria criteria, CriteriaBuilder cb, Root<T> root) {
        if (criteria.isIncludeFrom()) {
            if (criteria.isIncludeTo()) {
                return predicateNot(criteria, cb.between(root.get(criteria.getFieldName()), criteria.getFrom(), criteria.getTo()));
            } else {
                return predicateNot(criteria, cb.ge(root.get(criteria.getFieldName()), criteria.getFrom()));
            }
        }
        if (criteria.isIncludeTo()) {
            return predicateNot(criteria, cb.le(root.get(criteria.getFieldName()), criteria.getTo()));
        }
        return cb.isTrue(cb.literal(true));
    }

    protected Predicate buildExplicitSearchQuery(ExplicitSearchCriteria criteria, CriteriaBuilder cb, Root<T> root) {
        Predicate predicate;
        String fieldValue = criteria.getFieldValue().toString();

        if (fieldValue.contains("*")) {
            predicate = cb.like(root.get(criteria.getFieldName()), criteria.getFieldValue().toString().replace("*", ""));
        } else {
            predicate = cb.equal(root.get(criteria.getFieldName()), criteria.getFieldValue());
        }

        return predicateNot(criteria, predicate);
    }

    protected Predicate buildContainsSearchQuery(ContainsSearchCriteria criteria, CriteriaBuilder cb, Root<T> root) {
        if (criteria.getFieldValues().isEmpty()) {
            return cb.isTrue(cb.literal(true));
        }
        Predicate predicate = cb.equal(root.get(criteria.getFieldName()), criteria.getFieldValues().get(0));
        for (int i = 1; i < criteria.getFieldValues().size();i++) {
             predicate =  cb.or(predicate, cb.equal(root.get(criteria.getFieldName()), criteria.getFieldValues().get(i)));
        }

        return predicateNot(criteria, predicate);
    }

    protected Predicate buildAnyNoneQuery(ExistingFieldSearchCriteria criteria, CriteriaBuilder cb, Root<T> root) {
        return predicateNot(criteria, cb.isNotNull(root.get(criteria.getFieldName())));
    }

    protected Predicate buildCombinedQuery(CombinedSearchCriteria criteria, CriteriaBuilder cb, Root<T> root) {
        if (criteria.getSearchCriteriaList().isEmpty()) {
            return cb.isTrue(cb.literal(true));
        }
        Predicate predicate = buildInnerQuery(criteria.getSearchCriteriaList().get(0), cb,root);
        for (int i = 1; i < criteria.getSearchCriteriaList().size();i++) {
            if (CombinedSearchCriteria.Association.AND.equals(criteria.getAssociation())) {
                predicate = cb.and(buildInnerQuery(criteria.getSearchCriteriaList().get(i), cb,root));
            } else {
                predicate = cb.or(buildInnerQuery(criteria.getSearchCriteriaList().get(i), cb,root));
            }
        }

        return predicate;
    }

    protected Predicate predicateNot(FieldSearchCriteria fieldSearchCriteria, Predicate predicate) {
        if (fieldSearchCriteria.isNot()) {
            return predicate.not();
        }
        return predicate;
    }
}
