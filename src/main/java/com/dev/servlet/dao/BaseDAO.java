package com.dev.servlet.dao;

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
import java.io.Serializable;
import java.util.Collection;

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
        specialization = ClassUtil.getGenericType(this.getClass());
    }


    public T findById(E id) {
        return em.find(specialization, id);
    }

    public void save(T object) {
        try {
            this.beginTransaction();
            this.em.persist(object);
            this.commitTransaction();
            this.em.flush();
            this.em.clear();

        } catch (Exception e) {
            LOGGER.error("Error saving object: {}", e.getMessage());
            rollbackTransaction();
        }
    }

    public T update(T object) {
        try {
            this.beginTransaction();
            object = em.merge(object);
            this.commitTransaction();
            this.em.flush();
            this.em.clear();

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
     * @param ids        {@link Collection} of {@link E} product ids
     * @param pagination {@link Pagination}
     * @return {@link Collection} of {@link T}
     */
    public Collection<T> getAllPageable(Collection<E> ids, Pagination pagination) {
        //language=HQL
        String query = "SELECT p FROM " + specialization.getSimpleName() + " p WHERE p.identifier IN :ids ORDER BY p.id";
        query = query.replace("identifier", sanitize(getIdentifier()));

        TypedQuery<T> tTypedQuery = em.createQuery(query, specialization)
                .setParameter("ids", ids)
                .setFirstResult(pagination.getFirstResult())
                .setMaxResults(pagination.getPageSize());

        return tTypedQuery.getResultList();
    }

    /**
     * Get all results by ids
     *
     * @param ids {@link Collection} of {@link E} ids
     * @return {@link Collection} of {@link T} products
     */
    public Collection<T> getAllByIds(Collection<E> ids) {
        //language=HQL
        String query = "SELECT p FROM " + specialization.getSimpleName() + " p WHERE p.id IN :ids ORDER BY p.id";

        TypedQuery<T> typedQuery = em.createQuery(query, specialization);
        return typedQuery.setParameter("ids", ids).getResultList();
    }

    /**
     * Get the identifier
     *
     * @return {@link String} identifier
     */
    protected String getIdentifier() {
        return ID;
    }

    private CharSequence sanitize(String identifier) {
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
