package com.neo.javax.impl.persistence.entity;

import com.neo.common.impl.StopWatch;
import com.neo.common.impl.exception.InternalLogicException;
import com.neo.javax.api.persistence.entity.PersistenceContextService;
import com.neo.javax.api.persitence.entity.EntityQuery;
import com.neo.javax.api.persitence.entity.EntityResult;
import com.neo.javax.api.persitence.criteria.*;
import com.neo.javax.api.persitence.entity.DataBaseEntity;
import com.neo.javax.api.persitence.entity.EntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Transactional
@RequestScoped
public class EntityRepositoryImpl implements EntityRepository {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryImpl.class);

    @Inject PersistenceContextService pcs;

    @Override
    public void create(DataBaseEntity entity) {
        pcs.getEntityManager().persist(entity);
        LOGGER.debug("Created entity {}:{}", entity.getPrimaryKey(), entity.getClass().getSimpleName());
    }

    @Override
    public void edit(DataBaseEntity entity) {
        pcs.getEntityManager().merge(entity);
        LOGGER.debug("Edited entity {}:{}", entity.getPrimaryKey(), entity.getClass().getSimpleName());
    }

    @Override
    public void remove(DataBaseEntity entity) {
        pcs.getEntityManager().remove(pcs.getEntityManager().merge(entity));
        LOGGER.debug("Removed entity {}:{}", entity.getPrimaryKey(), entity.getClass().getSimpleName());
    }

    @Override
    public <X extends DataBaseEntity> Optional<X> find(Object primaryKey, Class<X> entityClazz) {
        try {
            LOGGER.trace("Searching for entity {}:{}", primaryKey, entityClazz.getSimpleName());
            return Optional.of(pcs.getEntityManager().find(entityClazz, primaryKey));
        } catch (NoResultException | IllegalArgumentException  ex) {
            LOGGER.trace("Unable to find entity {}:{}", primaryKey, entityClazz.getSimpleName());
            return Optional.empty();
        }
    }

    @Override
    public <X extends DataBaseEntity> EntityResult<X> find(EntityQuery<X> parameters) {
        LOGGER.trace("Searching for entity {} maxResults {} SearchCriteria {}",
                parameters.getEntityClass().getSimpleName(),
                parameters.getMaxResults().orElse(-1),
                parameters.getFilters().size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CriteriaBuilder cb = pcs.getEntityManager().getCriteriaBuilder();

        AbstractQuery<X> aQuery = cb.createQuery(parameters.getEntityClass());

        Root<X> root = aQuery.from(parameters.getEntityClass());

        aQuery.where(addSearchFilters(parameters.getFilters(), cb, root));

        CriteriaQuery<X> cQuery = ((CriteriaQuery<X>) aQuery).select(root);
        cQuery.orderBy(mapOrders(parameters.getSorting(), cb, root));

        if (parameters.getFields().isPresent()) {
            cQuery.multiselect(buildSelection(parameters.getFields().get(), root));
        }

        TypedQuery<X> typedQuery = pcs.getEntityManager().createQuery(cQuery);

        if (parameters.getMaxResults().isPresent()) {
            typedQuery = typedQuery.setMaxResults(parameters.getMaxResults().get());
        }

        typedQuery.setFirstResult(parameters.getOffset());

        EntityResult<X> entityResult = new EntityResult<>();
        entityResult.setHits(typedQuery.getResultList());
        if (parameters.getMaxResults().isEmpty()) {
            entityResult.setHitSize(entityResult.getHits().size());
        } else {
            entityResult.setHitSize(count(parameters.getFilters(),parameters.getEntityClass()));
        }
        stopWatch.stop();
        entityResult.setTookInMillis(stopWatch.getElapsedTimeMs());

        LOGGER.trace("Search result for entity {} hits {} tookInMilli {}",
                parameters.getEntityClass().getSimpleName(),
                entityResult.getHitSize(),
                entityResult.getTookInMillis());
        return entityResult;
    }

    public <X> int count(List<? extends SearchCriteria> filters, Class<X> entityClass) {
        CriteriaBuilder cb = pcs.getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<X> root = cq.from(entityClass);
        cq.select(cb.count(root));
        Query q = pcs.getEntityManager().createQuery(cq);
        cq.where(addSearchFilters(filters, cb, root));
        return ((Long) q.getSingleResult()).intValue();
    }

    protected List<Selection<?>> buildSelection(List<String> fields, Root<?> root) {
        List<Selection<?>> selectionList = new ArrayList<>();
        for (String field: fields) {
            selectionList.add(root.get(field));
        }
        return selectionList;
    }

    protected List<Order> mapOrders(Map<String, Boolean> orders, CriteriaBuilder cb, Root<?> root) {
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

    protected Predicate addSearchFilters(List<? extends SearchCriteria> searchFilters, CriteriaBuilder cb, Root<?> root) {
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

    protected Predicate buildInnerQuery(SearchCriteria filter, CriteriaBuilder cb, Root<?> root) {
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

    protected Predicate buildRangeQuery(LongRangeSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
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

    protected Predicate buildRangeQuery(DoubleRangeSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
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

    protected Predicate buildExplicitSearchQuery(ExplicitSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
        Predicate predicate;
        String fieldValue = criteria.getFieldValue().toString();

        if (fieldValue.contains("*")) {
            predicate = cb.like(root.get(criteria.getFieldName()), criteria.getFieldValue().toString().replace("*", ""));
        } else {
            predicate = cb.equal(root.get(criteria.getFieldName()), criteria.getFieldValue());
        }

        return predicateNot(criteria, predicate);
    }

    protected Predicate buildContainsSearchQuery(ContainsSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
        if (criteria.getFieldValues().isEmpty()) {
            return cb.isTrue(cb.literal(true));
        }
        Predicate predicate = cb.equal(root.get(criteria.getFieldName()), criteria.getFieldValues().get(0));
        for (int i = 1; i < criteria.getFieldValues().size();i++) {
            predicate =  cb.or(predicate, cb.equal(root.get(criteria.getFieldName()), criteria.getFieldValues().get(i)));
        }

        return predicateNot(criteria, predicate);
    }

    protected Predicate buildAnyNoneQuery(ExistingFieldSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
        return predicateNot(criteria, cb.isNotNull(root.get(criteria.getFieldName())));
    }

    protected Predicate buildCombinedQuery(CombinedSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
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