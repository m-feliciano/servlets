package com.dev.servlet.dao;

import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.utils.CollectionUtils;
import lombok.NoArgsConstructor;
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

@NoArgsConstructor
@RequestScoped
public class CategoryDAO extends BaseDAO<Category, Long> {

    @Override
    public List<Category> findAll(Category category) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Category> cq = cb.createQuery(Category.class);
        Root<Category> root = cq.from(Category.class);

        Predicate predicate = cb.equal(root.get(STATUS), StatusEnum.ACTIVE.getValue());
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
    public void delete(Category category) {
        Session session = getNewOpenSession();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Category> cu = builder.createCriteriaUpdate(Category.class);
        Root<Category> root = cu.from(Category.class);
        cu.set(STATUS, StatusEnum.DELETED.getValue());

        Predicate predicate = builder.equal(root.get(ID), category.getId());
        predicate = builder.and(predicate,
                builder.equal(root.get(USER).get(ID), category.getUser().getId()));

        cu.where(predicate);
        Query query = em.createQuery(cu);
        query.executeUpdate();

        session.getTransaction().commit();
    }

    /**
     * Register a list of categories
     *
     * @param categories {@link List} of {@link Category}
     */
//    @Override
    public void saveAll(List<Category> categories) throws ServiceException {
        LOGGER.trace("");

        Session session = getNewOpenSession();

        AtomicReference<String> errors = new AtomicReference<>();

        session.doWork(connection -> {
            String copies = String.join(", ", Collections.nCopies(3, "?"));
            //language=SQL
            String sql = "INSERT INTO tb_category (name, status, user_id) VALUES (" + copies + ")";

            try (var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Category category : categories) {
                    ps.setString(1, category.getName());
                    ps.setString(2, StatusEnum.ACTIVE.getValue());
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
    }
}
