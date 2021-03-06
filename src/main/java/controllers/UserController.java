package controllers;

import dao.UserDao;
import domain.User;

import javax.persistence.EntityManager;
import java.util.Objects;

public class UserController {

    private final UserDao userDao;

    public UserController(EntityManager em) {
        this.userDao = new UserDao(em);
    }

    /**
     * Save.
     * call save method of userDao.
     * throws IllegalArgumentException if user is null
     *
     * @param user the user
     */

    public void save(User user) {
        if (Objects.isNull(user)) throw new IllegalArgumentException("The user must not be null.");
        this.userDao.save(user);
    }

    /**
     * Update.
     * call update method of userDao.
     *
     * @param user the user
     */

    public void update(User user) {
        this.userDao.update(user);
    }

    /**
     * Delete by id.
     * call delete method of userDao.
     *
     * @param id the id
     */

    public void delete(Long id) {
        this.userDao.delete(id);
    }

    /**
     * Find by id.
     * call findById method of userDao.
     *
     * @param id the id
     * @return the user
     */

    public User findById(Long id) {
        return this.userDao.findById(id);
    }

    /**
     * Find.
     *
     * @return the user or null if not found
     */

    public User findByLogin(String login) {
        return userDao.findByLogin(login);
    }

}
