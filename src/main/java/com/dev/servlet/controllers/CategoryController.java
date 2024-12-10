package com.dev.servlet.controllers;

import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.interfaces.Controller;
import com.dev.servlet.interfaces.IHttpResponse;
import com.dev.servlet.interfaces.RequestMapping;
import com.dev.servlet.model.CategoryModel;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import java.util.Collection;


@NoArgsConstructor
@Controller(path = "/category")
public final class CategoryController extends BaseController<Category, Long> {

    @Inject
    public CategoryController(CategoryModel categoryModel) {
        super(categoryModel);
    }

    private CategoryModel getModel() {
        return (CategoryModel) super.getBaseModel();
    }

    /**
     * Forward to the register form.
     *
     * @return the response {@link IHttpResponse} with the next path
     */
    @RequestMapping(value = NEW, method = "GET")
    public IHttpResponse<Void> forwardRegister() {

        return HttpResponse.ofNext(super.forwardTo("formCreateCategory"));
    }

    /**
     * Delete a category.
     *
     * @param request the request containing the category id
     * @return {@link IHttpResponse} with no content {@link Void}
     * @throws ServiceException if an error occurs during deletion
     */
    @RequestMapping(value = DELETE, method = "POST")
    public IHttpResponse<Void> delete(Request request) throws ServiceException {
        this.getModel().delete(request);

        return HttpResponse.ofNext(super.redirectTo(LIST));
    }

    /**
     * Edit a category.
     *
     * @param request the request containing the category id
     * @return the response {@link IHttpResponse} with the next path
     * @throws ServiceException if an error occurs during edition
     */
    @RequestMapping(value = EDIT, method = "GET")
    public IHttpResponse<CategoryDTO> edit(Request request) throws ServiceException {
        CategoryDTO category = this.getModel().listById(request);
        // OK
        return super.buildHttpResponse(200, category, super.forwardTo("formUpdateCategory"));
    }

    /**
     * Register a new category.
     *
     * @param request the request containing the category data
     * @return the response {@link IHttpResponse} with the next path
     * @throws ServiceException if an error occurs during registration
     */
    @RequestMapping(value = CREATE, method = "POST")
    public IHttpResponse<Void> register(Request request) throws ServiceException {
        CategoryDTO category = this.getModel().register(request);
        // Created
        return super.buildHttpResponse(201, null, super.redirectTo(category.getId()));
    }

    /**
     * Update a category.
     *
     * @param request the request containing the category data
     * @return the response {@link IHttpResponse} with the next path
     * @throws ServiceException if an error occurs during update
     */
    @RequestMapping(value = UPDATE, method = "POST")
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        CategoryDTO category = this.getModel().update(request);
        // No Content
        return super.buildHttpResponse(204, null, super.redirectTo(category.getId()));
    }

    /**
     * List all categories.
     *
     * @param request the request containing the category data
     * @return the response {@link IHttpResponse} with the next path
     */
    @RequestMapping(value = LIST, method = "GET")
    public IHttpResponse<Collection<CategoryDTO>> list(Request request) {
        Collection<CategoryDTO> categories = this.getModel().list(request);
        // OK
        return super.buildHttpResponse(200, categories, super.forwardTo("listCategories"));
    }

    /**
     * List a category by id.
     *
     * @param request the request containing the category id
     * @return the response {@link IHttpResponse} with the next path
     */
    @RequestMapping(value = "/{id}", method = "GET")
    public IHttpResponse<CategoryDTO> listById(Request request) throws ServiceException {
        CategoryDTO category = this.getModel().listById(request);
        // OK
        return super.buildHttpResponse(200, category, super.forwardTo("formListCategory"));
    }
}
