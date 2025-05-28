package com.dev.servlet.persistence.dao.base;

import com.dev.servlet.model.pojo.records.Sort;
import com.dev.servlet.persistence.IPageRequest;
import com.dev.servlet.util.ClassUtil;
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

/**
 * Base DAO
 *
 * @param <T> specialization
 * @param <E> identifier
 * @implNote You should extend this class and provide a specialization
 */
@Slf4j
@NoArgsConstructor
public abstract class BaseDAO<T, E> implements Serializable {

    protected static final String STATUS = "status";
    protected static final String USER = "user";
    protected static final String ID = "id";
    public static final String NOT_IMPLEMENTED = "Not implemented";

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


    public T findById(E id) {
        return em.find(specialization, id);
    }

    public void save(T object) {
        try {
            this.beginTransaction();
            em.persist(object);
            this.em.flush();
            this.commitTransaction();

        } catch (Exception e) {
            log.error("Error saving object: {}", e.getMessage());
            rollbackTransaction();
        }

    }

    public T update(T object) {
        try {
            this.beginTransaction();
            object = em.merge(object);
            this.em.flush();
            this.commitTransaction();

            return object;
        } catch (Exception e) {
            log.error("Error updating object: {}", e.getMessage());
            rollbackTransaction();
        }
        return null;
    }

    public void delete(T object) {
        this.em.remove(object);
    }

    public T find(T object) {
        return em.find(specialization, object);
    }

    protected void beginTransaction() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    protected void commitTransaction() {
        try {
            beginTransaction();
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Error committing transaction: {}", e.getMessage());
            rollbackTransaction();
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

    /**
     * Get a new opened session
     *
     * @return {@linkplain Session}
     */
    protected Session getNewOpenSession() {
        Session session = em.unwrap(Session.class);
        session.beginTransaction();
        return session;
    }


    /**
     * Get the id of all results
     *
     * @param filter {@linkplain T} specialization
     * @return {@linkplain Collection} of {@linkplain E} identifiers of objects
     * @throws UnsupportedOperationException if not implemented
     */
    public Collection<E> findAllOnlyIds(T filter) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    public abstract Collection<T> findAll(T object);

    /**
     * Build a default filter for the given filter object
     *
     * @param filter  {@linkplain T} specialization
     * @param cb {@linkplain CriteriaBuilder}
     * @param root    {@linkplain Root} of the query
     * @return {@linkplain Predicate} for filtering
     */
    protected abstract Predicate buildDefaultPredicateFor(T filter, CriteriaBuilder cb, Root<?> root);

    /**
     * Get all results with pagination
     *
     * @param pageRequest {@linkplain IPageRequest}
     * @return {@linkplain Collection} of {@linkplain T}
     */
    public Iterable<T> getAllPageable(IPageRequest<T> pageRequest) {
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
}
