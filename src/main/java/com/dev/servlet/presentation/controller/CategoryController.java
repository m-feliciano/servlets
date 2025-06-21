package com.dev.servlet.presentation.controller;

import com.dev.servlet.application.transfer.dto.CategoryDTO;
import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.application.transfer.response.IHttpResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.pojo.enums.RequestMethod;
import com.dev.servlet.domain.service.CategoryService;
import com.dev.servlet.presentation.controller.base.BaseController;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@NoArgsConstructor
@Singleton
@Controller("category")
public class CategoryController extends BaseController {

    private CategoryService categoryService;

    @Inject
    public CategoryController(@NotNull CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Forward to the register form.
     *
     * @return the response {@linkplain IHttpResponse} with the next path
     */
    @RequestMapping(value = "/new")
    public IHttpResponse<Void> forwardRegister() {
        String next = forwardTo("formCreateCategory");
        return HttpResponse.<Void>ok().next(next).build();
    }

    /**
     * Delete a category.
     *
     * @param request the request containing the category id
     * @return {@linkplain IHttpResponse} with no content {@linkplain Void}
     * @throws ServiceException if an error occurs during deletion
     */
    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> delete(Request request) throws ServiceException {
        categoryService.delete(request);
        return HttpResponse.<Void>ok().next(redirectTo(LIST)).build();
    }

    /**
     * Edit a category.
     *
     * @param request the request containing the category id
     * @return the response {@linkplain IHttpResponse} with the next path
     * @throws ServiceException if an error occurs during edition
     */
    @RequestMapping(
            value = "/edit/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<CategoryDTO> edit(Request request) throws ServiceException {
        CategoryDTO category = categoryService.listById(request);
        // OK
        return okHttpResponse(category, forwardTo("formUpdateCategory"));
    }

    /**
     * Register a new category.
     *
     * @param request the request containing the category data
     * @return the response {@linkplain IHttpResponse} with the next path
     * @throws ServiceException if an error occurs during registration
     */
    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "name", constraints = {
                            @Constraints(minLength = 3, maxLength = 50, message = "Name must be between {0} and {1} characters")
                    })
            })
    public IHttpResponse<Void> register(Request request) throws ServiceException {
        CategoryDTO category = categoryService.register(request);
        // Created
        return newHttpResponse(201, redirectTo(category.getId()));
    }

    /**
     * Update a category.
     *
     * @param request the request containing the category data
     * @return the response {@linkplain IHttpResponse} with the next path
     * @throws ServiceException if an error occurs during update
     */
    @RequestMapping(
            value = "/update/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    }),
                    @Validator(values = "name", constraints = {
                            @Constraints(minLength = 5, maxLength = 50, message = "Name must be between {0} and {1} characters"),
                    })
            })
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        CategoryDTO category = categoryService.update(request);
        // No Content
        return newHttpResponse(204, redirectTo(category.getId()));
    }

    /**
     * List all categories.
     *
     * @param request the request containing the category data
     * @return the response {@linkplain IHttpResponse} with the next path
     */
    @RequestMapping(value = "/list")
    public IHttpResponse<Collection<CategoryDTO>> list(Request request) {
        Collection<CategoryDTO> categories = categoryService.list(request);
        // OK
        return okHttpResponse(categories, forwardTo("listCategories"));
    }

    /**
     * List a category by id.
     *
     * @param request the request containing the category id
     * @return the response {@linkplain IHttpResponse} with the next path
     */
    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<CategoryDTO> listById(Request request) throws ServiceException {
        CategoryDTO category = categoryService.listById(request);
        // OK
        return okHttpResponse(category, forwardTo("formListCategory"));
    }
}

