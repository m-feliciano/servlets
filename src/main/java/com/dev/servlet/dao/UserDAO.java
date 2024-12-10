package com.dev.servlet.dao;

import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.utils.CollectionUtils;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import javax.enterprise.context.RequestScoped;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@NoArgsConstructor
@RequestScoped
public class UserDAO extends BaseDAO<User, Long> {

    public static final String LOGIN = "login";

    @Override
    public List<User> findAll(User user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class).distinct(true);
        Root<User> root = cq.from(User.class);
        Predicate predicate = cb.notEqual(root.get(STATUS), StatusEnum.DELETED.getValue());
        predicate = cb.and(predicate, cb.equal(root.get(LOGIN), user.getLogin()));

        if (user.getPassword() != null) {
            predicate = cb.and(predicate, cb.equal(root.get("password"), user.getPassword()));
        }

        Order descId = cb.asc(root.get(ID));
        cq.select(root).where(predicate).orderBy(descId);

        TypedQuery<User> query = em.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public User find(User user) {
        List<User> all = findAll(user);
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return all.get(0);
    }

    @Override
    public void delete(User user) {
        Session session = getNewOpenSession();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<User> cu = builder.createCriteriaUpdate(User.class);
        Root<User> root = cu.from(User.class);
        cu.set(STATUS, StatusEnum.DELETED.getValue());
        Predicate predicate = builder.equal(root.get(ID), user.getId());
        cu.where(predicate);
        Query query = em.createQuery(cu);
        query.executeUpdate();

        session.getTransaction().commit();
    }
}
