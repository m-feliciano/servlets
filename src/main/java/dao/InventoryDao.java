package dao;

import domain.Inventory;

import javax.persistence.EntityManager;
import java.util.List;

public class InventoryDao {

    private final EntityManager em;

    public InventoryDao(EntityManager em) {
        this.em = em;
    }

    public void save(Inventory item) {
        this.em.getTransaction().begin();
        this.em.persist(item);
        this.em.getTransaction().commit();
    }

    public void update(Inventory item) {
        this.em.getTransaction().begin();
        this.em.merge(item);
        this.em.getTransaction().commit();
    }

    public boolean delete(Long id) {
        Inventory item = this.findById(id);
        if (item != null) {
            this.em.getTransaction().begin();
            this.em.remove(item);
            this.em.getTransaction().commit();
            return true;
        }
        return false;
    }

    public Inventory findById(Long id) {
        return this.em.find(Inventory.class, id);
    }

    public List<Inventory> findAll() {
        String jpsl = "SELECT p FROM Inventory p";
        return em.createQuery(jpsl, Inventory.class).getResultList();
    }

    public List<Inventory> findAllByDescription(String description) {
        String jsql = "SELECT i FROM Inventory i WHERE i.description = :description";
        return em.createQuery(jsql, Inventory.class).setParameter("description", description).getResultList();
    }

    public List<Inventory> findAllByProductName(String name) {
        String jsql = "SELECT i FROM Inventory i WHERE i.product.name = :name";
        return em.createQuery(jsql, Inventory.class).setParameter("name", name).getResultList();
    }

}