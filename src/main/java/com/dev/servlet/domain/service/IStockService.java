package com.dev.servlet.domain.service;
import com.dev.servlet.domain.transfer.dto.InventoryDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.Inventory;
import java.util.List;

/**
 * Service interface for managing inventory and stock operations in the servlet application.
 * 
 * <p>This interface defines the contract for all stock-related business operations,
 * including inventory management, stock level monitoring, and availability checking.
 * It handles the relationship between products and their available quantities,
 * ensuring proper stock control and inventory tracking throughout the system.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface IStockService {
    
    /**
     * Creates a new inventory record based on the provided request data.
     *
     * @param request the request containing inventory creation information
     * @return InventoryDTO representing the newly created inventory record
     * @throws ServiceException if creation fails due to validation or business rule violations
     */
    InventoryDTO create(Request request) throws ServiceException;
    
    /**
     * Lists inventory records based on the request criteria.
     * May include filtering, sorting, or other criteria specified in the request.
     *
     * @param request the request containing listing criteria
     * @return List of InventoryDTO objects matching the criteria
     */
    List<InventoryDTO> list(Request request);
    
    /**
     * Finds an inventory record by its identifier from the request.
     *
     * @param request the request containing the inventory ID
     * @return InventoryDTO representing the found inventory record
     * @throws ServiceException if inventory is not found or request is invalid
     */
    InventoryDTO findById(Request request) throws ServiceException;
    
    /**
     * Updates an existing inventory record with new data from the request.
     *
     * @param request the request containing updated inventory data
     * @return InventoryDTO representing the updated inventory record
     * @throws ServiceException if update fails due to validation or business rule violations
     */
    InventoryDTO update(Request request) throws ServiceException;
    
    /**
     * Deletes an inventory record identified by the request data.
     *
     * @param request the request containing inventory deletion criteria
     * @return true if deletion was successful, false otherwise
     * @throws ServiceException if deletion fails due to business constraints
     */
    boolean delete(Request request) throws ServiceException;
    
    /**
     * Checks if the given inventory has available stock.
     * This method validates stock availability for business operations.
     *
     * @param inventory the inventory to check for availability
     * @return true if inventory has available stock, false otherwise
     */
    boolean hasInventory(Inventory inventory);
}
