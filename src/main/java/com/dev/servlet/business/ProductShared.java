package com.dev.servlet.business;

import com.dev.servlet.controllers.InventoryController;
import com.dev.servlet.controllers.ProductController;
import com.dev.servlet.domain.Inventory;
import com.dev.servlet.domain.Product;
import com.dev.servlet.dto.ProductDto;
import com.dev.servlet.mapper.ProductMapper;

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
public class ProductShared {

    @Inject
    private ProductController productController;
    @Inject
    private InventoryController inventoryController;

    public ProductShared() {
    }

    public ProductShared(ProductController productController,
                         InventoryController inventoryController) {
        this.productController = productController;
        this.inventoryController = inventoryController;
    }

    public boolean hasInventory(Inventory inventory) {
        return inventoryController.hasInventory(inventory);
    }

    public ProductDto find(Long productId) {
        Product entity = productController.find(new Product(productId));
        return ProductMapper.from(entity);
    }
}