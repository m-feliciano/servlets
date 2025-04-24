package com.dev.servlet.infrastructure.persistence.dao;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.CollectionUtils;
import com.dev.servlet.domain.model.Inventory;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.enums.Status;
import com.dev.servlet.infrastructure.persistence.dao.base.BaseDAO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@NoArgsConstructor
@RequestScoped
public class InventoryDAO extends BaseDAO<Inventory, Long> {
    public static final String PRODUCT = "product";
    @Override
    public Inventory find(Inventory inventory) {
        List<Inventory> all = findAll(inventory);
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return all.get(0);
    }
    @Override
    public List<Inventory> findAll(Inventory inventory) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
        Root<Inventory> root = cq.from(Inventory.class);
        Predicate predicate = cb.equal(root.get(STATUS), Status.ACTIVE.getValue());
        predicate = cb.and(predicate, cb.equal(root.get(USER).get(ID), inventory.getUser().getId()));
        if (inventory.getId() != null) {
            predicate = cb.and(predicate, cb.equal(root.get(ID), inventory.getId()));
        } else if (inventory.getDescription() != null) {
            Expression<String> upper = cb.upper(root.get("description"));
            Predicate like = cb.like(upper, MatchMode.ANYWHERE.toMatchString(inventory.getDescription().toUpperCase()));
            predicate = cb.and(predicate, like);
        }
        if (inventory.getProduct() != null) {
            if (inventory.getProduct().getId() != null) {
                Predicate pProduct = cb.equal(root.get(PRODUCT).get(ID), inventory.getProduct().getId());
                predicate = cb.and(predicate, pProduct);
            } else {
                Expression<String> upper = cb.upper(root.get(PRODUCT).get("name"));
                Predicate pProduct = cb.like(upper, MatchMode.START.toMatchString(inventory.getProduct().getName().toUpperCase()));
                if (inventory.getProduct().getCategory() != null) {
                    Predicate pCategory = cb.equal(root.get(PRODUCT).get("category").get(ID), inventory.getProduct().getCategory().getId());
                    pProduct = cb.and(pProduct, pCategory);
                }
                predicate = cb.and(predicate, pProduct);
            }
        }
        Order desc = cb.asc(root.get(ID));
        cq.select(root).where(predicate).orderBy(desc);
        TypedQuery<Inventory> typedQuery = em.createQuery(cq);
        List<Inventory> inventories = typedQuery.getResultList();
        if (CollectionUtils.isEmpty(inventories)) {
            return Collections.emptyList();
        }
        return inventories;
    }
    @Override
    public boolean delete(Inventory inventory) {
        Session session = getNewOpenSession();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Inventory> cu = builder.createCriteriaUpdate(Inventory.class);
        Root<Inventory> root = cu.from(Inventory.class);
        cu.set(STATUS, Status.DELETED.getValue());
        Predicate predicate = builder.equal(root.get(ID), inventory.getId());
        predicate = builder.and(predicate,
                builder.equal(root.get(USER).get(ID), inventory.getUser().getId()));
        cu.where(predicate);
        Query query = em.createQuery(cu);
        query.executeUpdate();
        session.getTransaction().commit();
        return true;
    }

    public boolean has(Inventory inventory) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Inventory> root = cq.from(Inventory.class);
        Join<Inventory, Product> joinProduct = root.join(PRODUCT, JoinType.LEFT);
        Join<Inventory, Product> joinUser = root.join(USER, JoinType.LEFT);
        Predicate predicate = cb.equal(root.get(STATUS), Status.ACTIVE.getValue());
        predicate = cb.and(predicate,
                cb.equal(joinProduct.get(ID), inventory.getProduct().getId()),
                cb.equal(joinUser.get(ID), inventory.getUser().getId()));
        cq.select(cb.count(root)).where(predicate);
        Long count = em.createQuery(cq).getSingleResult();
        return count > 0;
    }
    @Override
    public List<Inventory> save(List<Inventory> inventories) throws ServiceException {
        log.trace("");
        AtomicReference<String> errors = new AtomicReference<>();
        Session session = getNewOpenSession();
        session.doWork(connection -> {
            String copies = String.join(", ", Collections.nCopies(5, "?"));
            String sql = "INSERT INTO tb_inventory (id, description, product_id, user_id, status) VALUES (" + copies + ")";
            try (java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                for (Inventory inventory : inventories) {
                    ps.setLong(1, inventory.getId());
                    ps.setString(2, inventory.getDescription());
                    ps.setLong(3, inventory.getProduct().getId());
                    ps.setLong(4, inventory.getUser().getId());
                    ps.setString(5, Status.ACTIVE.getValue());
                    ps.addBatch();
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    int i = 0;
                    while (rs.next()) {
                        inventories.get(i).setId(rs.getLong(1));
                        i++;
                    }
                }
                ps.executeBatch();
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
        return inventories;
    }
    @Override
    protected Predicate buildDefaultPredicateFor(Inventory filter, CriteriaBuilder cb, Root<?> root) {
        Predicate predicate = cb.equal(root.get(STATUS), Status.ACTIVE.getValue());
        predicate = cb.and(predicate, cb.equal(root.get(USER).get(ID), filter.getUser().getId()));
        if (filter.getId() != null) {
            predicate = cb.and(predicate, cb.equal(root.get(ID), filter.getId()));
        }
        if (filter.getUser() != null) {
            predicate = cb.and(predicate, cb.equal(root.get(USER).get(ID), filter.getUser().getId()));
        }
        if (filter.getDescription() != null) {
            Expression<String> upper = cb.upper(root.get("description"));
            Predicate like = cb.like(upper, MatchMode.ANYWHERE.toMatchString(filter.getDescription().toUpperCase()));
            predicate = cb.and(predicate, like);
        }
        return predicate;
    }
}
