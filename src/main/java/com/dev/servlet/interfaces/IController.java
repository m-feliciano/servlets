package com.dev.servlet.interfaces;

import java.util.Collection;

/**
 * Interface to be implemented by all controllers
 *
 * @param <T>
 * @param <E>
 */
// THIS INTERFACE MUST BE WELL DOCUMENTED, SO WE DON'T NEED TO DOCUMENT ITS IMPLEMENTATIONS
public interface IController<T, E> {

    /**
     * Find an object by its id
     *
     * @param id {@link E}
     * @return {@link T}
     */
    T findById(E id);

    /**
     * Find an object by its attributes
     *
     * @param object {@link T}
     * @return {@link T}
     */
    T find(T object);

    /**
     * Find all objects by its attributes
     *
     * @param object {@link T}
     * @return {@link Collection<T>}
     */
    Collection<T> findAll(T object);

    /**
     * Save an object
     *
     * @param object {@link T}
     */
    void save(T object);

    /**
     * Update an object
     *
     * @param object {@link T}
     * @return {@link T}
     */
    T update(T object);

    /**
     * Delete an object
     *
     * @param object {@link T}
     */
    void delete(T object);
}
