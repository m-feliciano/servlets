package com.dev.servlet.domain.service;

import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing product operations in the servlet application.
 * Extends the base service interface to provide specialized product-related functionality.
 * 
 * <p>This interface defines the contract for all product-related business operations,
 * including CRUD operations, price calculations, bulk operations, and web scraping capabilities.
 * It serves as the main entry point for product management in the domain layer.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface IProductService extends IBaseService<Product, Long> {
    
    /**
     * Creates a new product based on the provided request data.
     *
     * @param request the request containing product creation data
     * @return ProductDTO representing the created product
     * @throws ServiceException if creation fails due to validation or business rule violations
     */
    ProductDTO create(Request request);
    
    /**
     * Saves a list of products in bulk operation with authorization validation.
     *
     * @param products the list of products to save
     * @param authorization the authorization token for the operation
     * @return List of saved Product entities
     * @throws ServiceException if save operation fails or authorization is invalid
     */
    List<Product> save(List<Product> products, String authorization) throws ServiceException;
    
    /**
     * Finds a product by its identifier from the request.
     *
     * @param request the request containing the product ID
     * @return ProductDTO representing the found product
     * @throws ServiceException if product is not found or request is invalid
     */
    ProductDTO findById(Request request) throws ServiceException;
    
    /**
     * Updates an existing product with new data from the request.
     *
     * @param request the request containing updated product data
     * @return ProductDTO representing the updated product
     * @throws ServiceException if update fails due to validation or business rule violations
     */
    ProductDTO update(Request request) throws ServiceException;
    
    /**
     * Deletes a product identified by the request data.
     *
     * @param request the request containing product deletion criteria
     * @return true if deletion was successful, false otherwise
     * @throws ServiceException if deletion fails due to business constraints
     */
    boolean delete(Request request) throws ServiceException;
    
    /**
     * Calculates the total price for a given product including any applicable discounts,
     * taxes, or special pricing rules.
     *
     * @param product the product for which to calculate the total price
     * @return BigDecimal representing the calculated total price
     */
    BigDecimal calculateTotalPriceFor(Product product);
    
    /**
     * Scrapes product data from an external URL and converts it to ProductDTO objects.
     * This method is used for data integration from external sources.
     *
     * @param request the request containing scraping parameters
     * @param url the URL to scrape product data from
     * @return Optional containing list of scraped ProductDTOs, empty if scraping fails
     */
    Optional<List<ProductDTO>> scrape(Request request, String url);
}
