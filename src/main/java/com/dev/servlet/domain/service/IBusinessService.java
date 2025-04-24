package com.dev.servlet.domain.service;
import com.dev.servlet.domain.model.Inventory;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.User;

/**
 * Business service interface providing core business logic operations.
 * 
 * <p>This interface encapsulates essential business rules and operations that
 * span across multiple domains in the application. It provides high-level
 * business operations that coordinate between different entities and enforce
 * business constraints and validations.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface IBusinessService {
    
    /**
     * Checks if the given inventory has available stock.
     * This method validates inventory availability for business operations.
     *
     * @param inventory the inventory to check for availability
     * @return true if inventory has available stock, false otherwise
     */
    boolean hasInventory(Inventory inventory);
    
    /**
     * Retrieves a product by its identifier with user context validation.
     * This method ensures that the user has appropriate permissions to access the product.
     *
     * @param id the unique identifier of the product
     * @param user the user requesting access to the product
     * @return the Product entity if found and user has access permissions
     */
    Product getProductById(Long id, User user);
}
