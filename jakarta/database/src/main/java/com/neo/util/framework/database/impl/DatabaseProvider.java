package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.StopWatch;
import com.neo.util.common.impl.enumeration.Association;
import com.neo.util.framework.api.persistence.criteria.*;
import com.neo.util.framework.api.persistence.entity.EntityProvider;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.api.PersistenceContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Transactional
@ApplicationScoped
public class DatabaseProvider implements EntityProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DatabaseProvider.class);

    protected final PersistenceContextProvider pcp;

    @Inject
    public DatabaseProvider(PersistenceContextProvider pcp) {
        this.pcp = pcp;
    }

    @Override
    public void create(PersistenceEntity entity) {
        pcp.getEm().persist(entity);
        LOGGER.debug("Created entity {}:{}", entity.getClass().getSimpleName(),  entity);
    }

    @Override
    public void edit(PersistenceEntity entity) {
        pcp.getEm().merge(entity);
        LOGGER.debug("Edited entity {}:{}",entity.getClass().getSimpleName(), entity);
    }

    @Override
    public void remove(PersistenceEntity entity) {
        pcp.getEm().remove(pcp.getEm().merge(entity));
        LOGGER.debug("Removed entity {}:{}",entity.getClass().getSimpleName(), entity);
    }

    @Override
    public <X extends PersistenceEntity> Optional<X> fetch(Object primaryKey, Class<X> entityClazz) {
        try {
            LOGGER.trace("Searching for entity {}:{}", entityClazz.getSimpleName(), primaryKey);
            return Optional.ofNullable(pcp.getEm().find(entityClazz, primaryKey));
        } catch (NoResultException | IllegalArgumentException  ex) {
            LOGGER.trace("Unable to find entity {}:{}", entityClazz.getSimpleName(), primaryKey);
            return Optional.empty();
        }
    }

    @Override
    public <X extends PersistenceEntity> EntityResult<X> fetch(EntityQuery<X> parameters) {
        LOGGER.trace("Searching for entity {} maxResults {} SearchCriteria {}",
                parameters.getEntityClass().getSimpleName(),
                parameters.getMaxResults().orElse(-1),
                parameters.getFilters().size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CriteriaBuilder cb = pcp.getEm().getCriteriaBuilder();

        CriteriaQuery<X> cQuery = cb.createQuery(parameters.getEntityClass());

        Root<X> root = cQuery.from(parameters.getEntityClass());

        cQuery.where(addSearchFilters(parameters.getFilters(), cb, root));

        cQuery.orderBy(mapOrders(parameters.getSorting(), cb, root));

        TypedQuery<X> typedQuery = pcp.getEm().createQuery(cQuery);

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
        CriteriaBuilder cb = pcp.getEm().getCriteriaBuilder();
        CriteriaQuery<Object> cq = cb.createQuery();
        Root<X> root = cq.from(entityClass);
        cq.select(cb.count(root));
        Query q = pcp.getEm().createQuery(cq);
        cq.where(addSearchFilters(filters, cb, root));
        return ((Long) q.getSingleResult()).intValue();
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
        return switch (filter) {
            case DateSearchCriteria criteria -> buildDateRangeQuery(criteria, cb, root);
            case DoubleRangeSearchCriteria criteria -> buildRangeQuery(criteria, cb, root);
            case LongRangeSearchCriteria criteria -> buildRangeQuery(criteria, cb, root);
            case ExplicitSearchCriteria criteria -> buildExplicitSearchQuery(criteria, cb, root);
            case ContainsSearchCriteria criteria -> buildContainsSearchQuery(criteria, cb, root);
            case ExistingFieldSearchCriteria criteria ->buildAnyNoneQuery(criteria, cb, root);
            case CombinedSearchCriteria criteria -> buildCombinedQuery(criteria, cb, root);
            default -> throw new IllegalStateException("Criteria not supported " + filter.getClass().getName());
        };
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

    protected Predicate buildDateRangeQuery(DateSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
        if (criteria.isIncludeFrom()) {
            if (criteria.isIncludeTo()) {
                return cb.between(root.get(criteria.getFieldName()), criteria.getFromDate(), criteria.getToDate());
            } else {
                return cb.greaterThanOrEqualTo(root.get(criteria.getFieldName()), criteria.getFromDate());
            }
        }
        if (criteria.isIncludeTo()) {
            return cb.lessThanOrEqualTo(root.get(criteria.getFieldName()), criteria.getToDate());
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
        if (criteria.getFieldValue() instanceof String fieldValue
                && criteria.getAllowWildcards() && (fieldValue.contains("*") || fieldValue.contains("?"))) {
            predicate = cb.like(root.get(criteria.getFieldName()),
                    fieldValue.replace('*','%').replace('?','_'));
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
        if (criteria.getExists() ^ criteria.isNot()) {
            return cb.isNotNull(root.get(criteria.getFieldName()));
        }
        return cb.isNotNull(root.get(criteria.getFieldName())).not();
    }

    protected Predicate buildCombinedQuery(CombinedSearchCriteria criteria, CriteriaBuilder cb, Root<?> root) {
        if (criteria.getSearchCriteriaList().isEmpty()) {
            return cb.isTrue(cb.literal(true));
        }
        Predicate predicate = buildInnerQuery(criteria.getSearchCriteriaList().get(0), cb,root);
        for (int i = 1; i < criteria.getSearchCriteriaList().size();i++) {
            if (Association.AND.equals(criteria.getAssociation())) {
                predicate = cb.and(buildInnerQuery(criteria.getSearchCriteriaList().get(i), cb,root), predicate);
            } else {
                predicate = cb.or(buildInnerQuery(criteria.getSearchCriteriaList().get(i), cb,root), predicate);
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
