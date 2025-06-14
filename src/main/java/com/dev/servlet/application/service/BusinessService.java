package com.dev.servlet.application.service;

import com.dev.servlet.domain.model.pojo.domain.Inventory;
import com.dev.servlet.domain.model.pojo.domain.Product;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.service.InventoryService;
import com.dev.servlet.domain.service.ProductService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Product shared.
 * <p>
 * This class is used to share the product between the product and inventory business.
 * And will not be used in the view layer.
 *
 * @since 1.3.0
 */
@Singleton
public class BusinessService {

    private ProductService productService;
    private InventoryService inventoryService;

    public BusinessService() {
        // Empty constructor
    }

    @Inject
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Inject
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public boolean hasInventory(Inventory inventory) {
        return inventoryService.hasInventory(inventory);
    }

    /**
     * Find a product by id.
     *
     * @param productId
     * @return {@linkplain Product}
     */
    public Product getProductById(Long productId, User user) {
        if (productId == null) return null;
        return productService.find(new Product(productId, user));
    }
}

