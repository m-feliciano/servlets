package com.dev.servlet.infrastructure.persistence.dao;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.CollectionUtils;
import com.dev.servlet.domain.model.Category;
import com.dev.servlet.domain.model.enums.Status;
import com.dev.servlet.infrastructure.persistence.dao.base.BaseDAO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import javax.enterprise.context.RequestScoped;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
@Slf4j
@NoArgsConstructor
@RequestScoped
public class CategoryDAO extends BaseDAO<Category, Long> {
    @Override
    public List<Category> findAll(Category category) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Category> cq = cb.createQuery(Category.class);
        Root<Category> root = cq.from(Category.class);
        Predicate predicate = cb.equal(root.get(STATUS), Status.ACTIVE.getValue());
        predicate = cb.and(predicate, cb.equal(root.get(USER), category.getUser()));

        if (category.getName() != null) {
            Expression<String> upper = cb.upper(root.get("name"));
            Predicate like = cb.like(upper, MatchMode.ANYWHERE.toMatchString(category.getName().toUpperCase()));
            predicate = cb.and(predicate, like);
        }

        Order desc = cb.asc(root.get(ID));
        cq.select(root).where(predicate).orderBy(desc);
        return em.createQuery(cq).getResultList();
    }

    @Override
    public Category find(Category category) {
        List<Category> all = findAll(category);
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return all.iterator().next();
    }

    @Override
    public boolean delete(Category category) {
        Session session = getNewOpenSession();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Category> cu = builder.createCriteriaUpdate(Category.class);
        Root<Category> root = cu.from(Category.class);
        cu.set(STATUS, Status.DELETED.getValue());
        Predicate predicate = builder.equal(root.get(ID), category.getId());
        predicate = builder.and(predicate,
                builder.equal(root.get(USER).get(ID), category.getUser().getId()));
        cu.where(predicate);
        Query query = em.createQuery(cu);
        query.executeUpdate();
        session.getTransaction().commit();
        return true;
    }

    @Override
    public List<Category> save(List<Category> categories) throws ServiceException {
        log.trace("");
        AtomicReference<String> errors = new AtomicReference<>();
        Session session = getNewOpenSession();
        session.doWork(connection -> {
            String copies = String.join(", ", Collections.nCopies(3, "?"));
            String sql = "INSERT INTO tb_category (name, status, user_id) VALUES (" + copies + ")";
            try (var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Category category : categories) {
                    ps.setString(1, category.getName());
                    ps.setString(2, Status.ACTIVE.getValue());
                    ps.setLong(3, category.getUser().getId());
                    ps.addBatch();
                }
                ps.executeBatch();
                try (var rs = ps.getGeneratedKeys()) {
                    int i = 0;
                    while (rs.next()) {
                        categories.get(i).setId(rs.getLong(1));
                        i++;
                    }
                }
            } catch (Exception e) {
                errors.set(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        });
        if (errors.get() != null) {
            throw new ServiceException(errors.get());
        }
        try {
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
        }
        return categories;
    }

    @Override
    protected Predicate buildDefaultPredicateFor(Category filter, CriteriaBuilder cb, Root<?> root) {
        Predicate predicate = cb.equal(root.get(STATUS), Status.ACTIVE.getValue());
        predicate = cb.and(predicate, cb.equal(root.get(USER), filter.getUser()));
        if (filter.getUser() != null) {
            predicate = cb.and(predicate, cb.equal(root.get(USER).get(ID), filter.getUser().getId()));
        }
        if (filter.getName() != null) {
            Expression<String> upper = cb.upper(root.get("name"));
            Predicate like = cb.like(upper, MatchMode.ANYWHERE.toMatchString(filter.getName().toUpperCase()));
            predicate = cb.and(predicate, like);
        }
        return predicate;
    }
}
