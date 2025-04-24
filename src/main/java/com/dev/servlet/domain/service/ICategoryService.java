package com.dev.servlet.domain.service;
import com.dev.servlet.domain.transfer.dto.CategoryDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import java.util.Collection;

/**
 * Service interface for managing category operations in the servlet application.
 * 
 * <p>This interface defines the contract for all category-related business operations,
 * including category management, hierarchical operations, and product categorization.
 * Categories are used to organize and classify products within the application,
 * providing a structured way to manage product taxonomy.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface ICategoryService {
    
    /**
     * Registers a new category in the system based on the provided request data.
     *
     * @param request the request containing category creation information
     * @return CategoryDTO representing the newly created category
     * @throws ServiceException if registration fails due to validation or business rule violations
     */
    CategoryDTO register(Request request) throws ServiceException;
    
    /**
     * Updates an existing category with new data from the request.
     *
     * @param request the request containing updated category data
     * @return CategoryDTO representing the updated category
     * @throws ServiceException if update fails due to validation or business rule violations
     */
    CategoryDTO update(Request request) throws ServiceException;
    
    /**
     * Retrieves a category by its identifier from the request.
     *
     * @param request the request containing the category ID
     * @return CategoryDTO representing the found category
     * @throws ServiceException if category is not found or request is invalid
     */
    CategoryDTO getById(Request request) throws ServiceException;
    
    /**
     * Lists all categories based on the request criteria.
     * May include filtering, sorting, or other criteria specified in the request.
     *
     * @param request the request containing listing criteria
     * @return Collection of CategoryDTO objects matching the criteria
     */
    Collection<CategoryDTO> list(Request request);
    
    /**
     * Deletes a category identified by the request data.
     *
     * @param request the request containing category deletion criteria
     * @return true if deletion was successful, false otherwise
     * @throws ServiceException if deletion fails due to business constraints or dependencies
     */
    boolean delete(Request request) throws ServiceException;
}
