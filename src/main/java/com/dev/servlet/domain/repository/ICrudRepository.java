package com.dev.servlet.domain.repository;

import com.dev.servlet.domain.model.Identifier;

import java.util.Collection;

/**
 * Interface to be implemented by all models.
 *
 * @param <T> the type of the object
 * @param <K> the type of the object's identifier
 */
public interface ICrudRepository<T extends Identifier<K>, K> extends IPagination<T> {

    /**
     * Find an object by its identifier.
     *
     * @param id the identifier
     * @return the object
     */
    T findById(K id);

    /**
     * Find an object by its properties.
     *
     * @param object the object
     * @return the object
     */
    T find(T object);

    /**
     * Find all objects by their properties.
     *
     * @param object the object
     * @return the collection of objects
     */
    Collection<T> findAll(T object);

    /**
     * Save an object.
     *
     * @param object the object
     */
    T save(T object);

    /**
     * Update an object.
     *
     * @param object the object
     * @return the updated object
     */
    T update(T object);

    /**
     * Delete an object.
     *
     * @param object the object
     */
    void delete(T object);
}