package com.dev.servlet.dao;

import com.dev.servlet.pojo.enums.Order;
import com.dev.servlet.pojo.records.Pagination;
import com.dev.servlet.utils.ClassUtil;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
@NoArgsConstructor
public abstract class BaseDAO<T, E> implements Serializable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseDAO.class);
    protected static final String STATUS = "status";
    protected static final String USER = "user";
    protected static final String ID = "id";
    public static final String NOT_IMPLEMENTED = "Not implemented";

    @Inject
    protected EntityManager em;
    private Class<T> specialization;

    protected BaseDAO(EntityManager em) {
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
            LOGGER.error("Error saving object: {}", e.getMessage());
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
            LOGGER.error("Error updating object: {}", e.getMessage());
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
            LOGGER.error("Error committing transaction: {}", e.getMessage());
            rollbackTransaction();
        }
    }

    protected void rollbackTransaction() {
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            LOGGER.error("Error rolling back transaction: {}", e.getMessage());
        }
    }

    /**
     * Get a new opened session
     *
     * @return {@link Session}
     */
    protected Session getNewOpenSession() {
        Session session = em.unwrap(Session.class);
        session.beginTransaction();
        return session;
    }

    /**
     * Get all results with pagination
     *
     * @param identifiers {@link Collection} of {@link E} identifiers
     * @param pagination  {@link Pagination}
     * @return {@link Collection} of {@link T}
     */
    public Collection<T> getAllPageable(Collection<E> identifiers, Pagination pagination) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(specialization);
        Root<T> root = cq.from(specialization);

        String identifier = sanitize(getIdentifier());
        cq.select(root).where(root.get(identifier).in(identifiers));

        Order order = Order.ASC;
        if (pagination.getOrder() != null) {
            order = pagination.getOrder();
        }

        if (pagination.getSort() != null) {
            cq.orderBy(Order.DESC.equals(order)
                    ? cb.desc(root.get(pagination.getSort().getValue()))
                    : cb.asc(root.get(pagination.getSort().getValue())));
        } else {
            cq.orderBy(cb.asc(root.get(identifier)));
        }

        TypedQuery<T> typedQuery = em.createQuery(cq)
                .setFirstResult(pagination.getFirstResult())
                .setMaxResults(pagination.getPageSize());

        return typedQuery.getResultList();
    }

    /**
     * Get all results by ids
     *
     * @param ids {@link Collection} of {@link E} ids
     * @return {@link Collection} of {@link T} products
     */
    public Collection<T> getAllByIds(Collection<E> ids) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(specialization);
        Root<T> root = cq.from(specialization);

        String identifier = sanitize(getIdentifier());
        cq.select(root).where(root.get(identifier).in(ids));

        TypedQuery<T> typedQuery = em.createQuery(cq);
        return typedQuery.getResultList();
    }

    /**
     * Get the identifier
     *
     * @return {@link String} identifier
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
     * @param filter {@link T} specialization
     * @return {@link Collection} of {@link E} identifiers of objects
     * @throws UnsupportedOperationException if not implemented
     */
    public Collection<E> findAllOnlyIds(T filter) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Find all
     *
     * @param object {@link T}
     * @return {@link Collection} of {@link T}
     */
    public abstract Collection<T> findAll(T object);
}
