package com.dev.servlet.controllers;

import com.dev.servlet.dao.ProductDAO;
import com.dev.servlet.pojo.Product;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collection;

@NoArgsConstructor
public final class ProductController extends BaseController<Product, Long> {

    @Inject
    public ProductController(ProductDAO productDAO) {
        super(productDAO);
    }

    @Override
    protected ProductDAO getBaseDAO() {
        return (ProductDAO) super.getBaseDAO();
    }

    public BigDecimal calculateTotalPrice(Collection<Long> productIds) {
        return this.getBaseDAO().calculateTotalPrice(productIds);
    }
}
