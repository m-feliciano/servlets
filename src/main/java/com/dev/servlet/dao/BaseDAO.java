package com.dev.servlet.dao;

import com.dev.servlet.pojo.Pageable;
import com.dev.servlet.pojo.records.Sort;
import com.dev.servlet.utils.ClassUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Session;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
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
     * Get all results with pagination
     *
     * @param pageable {@linkplain Pageable}
     * @return {@linkplain Collection} of {@linkplain T}
     */
    public Collection<T> getAllPageable(Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(specialization);
        Root<T> root = cq.from(specialization);

        String identifier = sanitize(getIdentifier());
        cq.select(root).where(root.get(identifier).in(pageable.getRecords()));

        if (pageable.getSort() != null) {
            Sort.Direction direction = ObjectUtils.defaultIfNull(pageable.getSort().direction(), Sort.Direction.ASC);

            Path<Object> path = root.get(pageable.getSort().field());
            cq.orderBy(Sort.Direction.DESC.equals(direction) ? cb.desc(path) : cb.asc(path));
        } else {
            cq.orderBy(cb.asc(root.get(identifier)));
        }

        TypedQuery<T> typedQuery = em.createQuery(cq)
                .setFirstResult(pageable.getFirstResult())
                .setMaxResults(pageable.getPageSize());

        return typedQuery.getResultList();
    }

    /**
     * Get the identifier
     *
     * @return identifier
     */
    protected String getIdentifier() {
        return ID;
    }

    private String sanitize(String identifier) {
        return identifier.replaceAll("[^a-zA-Z0-9]", "");
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

    /**
     * Find all
     *
     * @param object {@linkplain T}
     * @return {@linkplain Collection} of {@linkplain T}
     */
    public abstract Collection<T> findAll(T object);
}
