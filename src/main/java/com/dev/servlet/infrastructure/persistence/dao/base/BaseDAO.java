package com.dev.servlet.infrastructure.persistence.dao.base;

import com.dev.servlet.domain.transfer.records.Sort;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.ClassUtil;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
@Slf4j
@NoArgsConstructor
public abstract class BaseDAO<T, ID> implements Serializable {
    protected static final String STATUS = "status";
    protected static final String USER = "user";
    protected static final String ID = "id";
    protected EntityManager em;
    private Class<T> specialization;

    @Inject
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @PostConstruct
    public void init() {
        specialization = ClassUtil.getSubClassType(this.getClass());
    }

    public T findById(ID id) {
        return em.find(specialization, id);
    }

    public T find(T object) {
        return em.find(specialization, object);
    }

    public T save(T object) {
        return executeInTransaction(() -> {
            em.persist(object);
            return object;
        });
    }

    public List<T> save(List<T> products) throws ServiceException {
        return executeInTransaction(() -> {
            for (T product : products) {
                em.persist(product);
            }
            return products;
        });
    }

    public T update(T object) {
        return executeInTransaction(() -> em.merge(object));
    }

    public boolean delete(T object) {
        executeInTransaction(() -> {
            em.remove(object);
            return true;
        });
        return false;
    }

    private void beginTransaction() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    protected void commitTransaction() {
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Error committing transaction: {}", e.getMessage());
            rollbackTransaction();
            throw e;
        }
    }

    protected void rollbackTransaction() {
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            log.error("Error rolling back transaction: {}", e.getMessage());
        }
    }

    private <R> R executeInTransaction(TransactionAction<R> action) {
        try {
            beginTransaction();
            R result = action.execute();
            commitTransaction();
            return result;
        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage());
            rollbackTransaction();
            throw new RuntimeException("Transaction failed", e);
        }
    }

    protected Session getNewOpenSession() {
        Session session = em.unwrap(Session.class);
        session.beginTransaction();
        return session;
    }

    public abstract Collection<T> findAll(T object);
    protected abstract Predicate buildDefaultPredicateFor(T filter, CriteriaBuilder cb, Root<?> root);
    public List<T> getAllPageable(IPageRequest<T> pageRequest) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(specialization);
        Root<T> root = query.from(specialization);
        if (pageRequest.getSort() != null && pageRequest.getSort().getField() != null) {
            Path<Object> path = root.get(pageRequest.getSort().getField());
            query.orderBy(pageRequest.getSort().getDirection() == Sort.Direction.DESC ? cb.desc(path) : cb.asc(path));
        }
        Predicate predicate = buildDefaultPredicateFor(pageRequest.getFilter(), cb, root);
        query.where(predicate).select(root).distinct(true);
        TypedQuery<T> typedQuery = em.createQuery(query)
                .setFirstResult(pageRequest.getFirstResult())
                .setMaxResults(pageRequest.getPageSize());
        return typedQuery.getResultList();
    }

    public long count(IPageRequest<T> pageRequest) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(specialization);
        Predicate predicate = buildDefaultPredicateFor(pageRequest.getFilter(), cb, root);
        query.where(predicate).select(cb.count(root));
        TypedQuery<Long> typedQuery = em.createQuery(query);
        Long count = typedQuery.getSingleResult();
        return count != null ? count : 0L;
    }
    @FunctionalInterface
    private interface TransactionAction<R> {
        R execute();
    }
}
