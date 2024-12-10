package com.dev.servlet.business.shared;

import com.dev.servlet.controllers.InventoryController;
import com.dev.servlet.controllers.ProductController;
import com.dev.servlet.pojo.Inventory;
import com.dev.servlet.pojo.Product;

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
public class BusinessShared {
    @Inject
    private ProductController productController;
    @Inject
    private InventoryController inventoryController;

    public BusinessShared() {
        // Empty constructor
    }

    public boolean hasInventory(Inventory inventory) {
        return inventoryController.has(inventory);
    }

    /**
     * Find a product by id.
     *
     * @param productId
     * @return {@link Product}
     */
    public Product getProductById(Long productId) {
        if (productId == null) return null;

        return productController.find(new Product(productId));
    }
}
