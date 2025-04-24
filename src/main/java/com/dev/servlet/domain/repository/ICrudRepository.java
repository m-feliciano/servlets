package com.dev.servlet.domain.repository;
import java.util.Collection;

/**
 * Generic CRUD repository interface that provides basic data access operations
 * for any entity type. Extends pagination capabilities for large datasets.
 * 
 * <p>This interface follows the Repository pattern and defines the contract for
 * all data access operations including Create, Read, Update, and Delete (CRUD).
 * It serves as the base interface for all repository implementations in the application,
 * providing consistent data access patterns across different entity types.</p>
 * 
 * @param <T> the entity type this repository manages
 * @param <ID> the type of the entity's identifier
 * @author servlets-team
 * @since 1.0
 */
public interface ICrudRepository<T, ID> extends IPagination<T> {
    
    /**
     * Finds an entity by its unique identifier.
     *
     * @param id the unique identifier of the entity to find
     * @return the entity with the specified ID, or null if not found
     */
    T findById(ID id);
    
    /**
     * Finds an entity that matches the provided entity's properties.
     * Uses the entity's properties as search criteria.
     *
     * @param object the entity containing search criteria
     * @return the matching entity, or null if not found
     */
    T find(T object);
    
    /**
     * Finds all entities that match the provided entity's properties.
     * Uses the entity's properties as filter criteria.
     *
     * @param object the entity containing filter criteria
     * @return collection of matching entities, empty collection if none found
     */
    Collection<T> findAll(T object);
    
    /**
     * Persists a new entity to the data store.
     *
     * @param object the entity to save
     * @return the saved entity with any generated values (like ID)
     */
    T save(T object);
    
    /**
     * Updates an existing entity in the data store.
     *
     * @param object the entity to update with new values
     * @return the updated entity
     */
    T update(T object);
    
    /**
     * Removes an entity from the data store.
     *
     * @param object the entity to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(T object);
}
