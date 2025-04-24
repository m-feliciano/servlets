package com.dev.servlet.infrastructure.persistence.dao;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.CollectionUtils;
import com.dev.servlet.domain.model.Category;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.enums.Status;
import com.dev.servlet.infrastructure.persistence.dao.base.BaseDAO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import javax.enterprise.context.RequestScoped;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@NoArgsConstructor
@RequestScoped
public class ProductDAO extends BaseDAO<Product, Long> {
    private static Predicate buildDefaultFilter(Product product, CriteriaBuilder criteriaBuilder, Root<Product> root) {
        Predicate predicate = criteriaBuilder.notEqual(root.get(STATUS), Status.DELETED.getValue());
        if (product.getUser() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(USER), product.getUser()));
        }
        if (product.getId() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(ID), product.getId()));
        } else {
            if (product.getName() != null) {
                Expression<String> upper = criteriaBuilder.upper(root.get("name"));
                Predicate like = criteriaBuilder.like(upper, MatchMode.START.toMatchString(product.getName().toUpperCase()));
                predicate = criteriaBuilder.and(predicate, like);
            }
            if (product.getDescription() != null) {
                Expression<String> upper = criteriaBuilder.upper(root.get("description"));
                Predicate like = criteriaBuilder.like(upper, MatchMode.ANYWHERE.toMatchString(product.getDescription().toUpperCase()));
                predicate = criteriaBuilder.and(predicate, like);
            }
            if (product.getCategory() != null) {
                Join<Product, Category> join = root.join("category", JoinType.INNER);
                if (product.getCategory().getId() != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(join.get(ID), product.getCategory().getId()));
                } else {
                    if (product.getCategory().getName() != null) {
                        Expression<String> upper = criteriaBuilder.upper(join.get("name"));
                        Predicate like = criteriaBuilder.like(upper, MatchMode.START.toMatchString(product.getCategory().getName().toUpperCase()));
                        predicate = criteriaBuilder.and(predicate, like);
                    }
                }
            }
        }
        return predicate;
    }
    @Override
    public Product find(Product product) {
        List<Product> all = findAll(product);
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return all.get(0);
    }
    @Override
    public List<Product> findAll(Product product) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class).distinct(true);
        Root<Product> root = query.from(Product.class);
        Predicate predicate = buildDefaultFilter(product, cb, root);
        javax.persistence.criteria.Order descId = cb.asc(root.get(ID));
        query.where(predicate).select(root).orderBy(descId);
        TypedQuery<Product> typedQuery = em.createQuery(query);
        List<Product> resultList = typedQuery.getResultList();
        if (!CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }
        return Collections.emptyList();
    }
    @Override
    public boolean delete(Product product) {
        Session session = getNewOpenSession();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Product> cu = builder.createCriteriaUpdate(Product.class);
        Root<Product> root = cu.from(Product.class);
        cu.set(STATUS, Status.DELETED.getValue());
        Predicate predicate = builder.equal(root.get(ID), product.getId());
        predicate = builder.and(predicate, builder.equal(root.get(USER).get(ID), product.getUser().getId()));
        cu.where(predicate);
        Query query = em.createQuery(cu);
        query.executeUpdate();
        session.getTransaction().commit();
        return true;
    }
    @Override
    public List<Product> save(List<Product> products) throws ServiceException {
        log.trace("");
        AtomicReference<String> errors = new AtomicReference<>();
        Session session = getNewOpenSession();
        session.doWork(connection -> {
            String copies = String.join(", ", Collections.nCopies(8, "?"));
            String sql = "INSERT INTO tb_product (name, description, url_img, register_date, price, user_id, status, category_id) VALUES (" + copies + ")";
            try (var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Product product : products) {
                    ps.setString(1, product.getName());
                    ps.setString(2, product.getDescription());
                    ps.setString(3, product.getUrl());
                    ps.setDate(4, new Date(product.getRegisterDate().getTime()));
                    ps.setBigDecimal(5, product.getPrice());
                    ps.setLong(6, product.getUser().getId());
                    ps.setString(7, Status.ACTIVE.getValue());
                    if (product.getCategory() != null) {
                        ps.setLong(8, product.getCategory().getId());
                    } else {
                        ps.setNull(8, java.sql.Types.BIGINT);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
                try (var rs = ps.getGeneratedKeys()) {
                    int i = 0;
                    while (rs.next()) {
                        products.get(i).setId(rs.getLong(1));
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
        return products;
    }

    public BigDecimal calculateTotalPriceFor(Product filter) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = builder.createQuery(BigDecimal.class);
        Root<Product> root = query.from(Product.class);
        Predicate predicate = buildDefaultFilter(filter, builder, root);
        query.where(predicate).select(builder.sum(root.get("price")));
        BigDecimal totalPrice = em.createQuery(query).getSingleResult();
        return ObjectUtils.defaultIfNull(totalPrice, BigDecimal.ZERO);
    }
    @Override
    @SuppressWarnings("unchecked")
    protected Predicate buildDefaultPredicateFor(Product filter, CriteriaBuilder cb, Root<?> root) {
        return buildDefaultFilter(filter, cb, (Root<Product>) root);
    }
}
