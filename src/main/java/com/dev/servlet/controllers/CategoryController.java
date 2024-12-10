package com.dev.servlet.controllers;

import com.dev.servlet.dao.CategoryDAO;
import com.dev.servlet.pojo.Category;
import lombok.NoArgsConstructor;

import javax.inject.Inject;


@NoArgsConstructor
public final class CategoryController extends BaseController<Category, Long> {

    @Inject
    public CategoryController(CategoryDAO categoryDAO) {
        super(categoryDAO);
    }

    @Override
    protected CategoryDAO getBaseDAO() {
        return (CategoryDAO) super.getBaseDAO();
    }
}
