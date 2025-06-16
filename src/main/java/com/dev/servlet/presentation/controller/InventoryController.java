package com.dev.servlet.presentation.controller;


import com.dev.servlet.application.transfer.dto.CategoryDTO;
import com.dev.servlet.application.transfer.dto.InventoryDTO;
import com.dev.servlet.application.transfer.records.KeyPair;
import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.application.transfer.response.IHttpResponse;
import com.dev.servlet.application.transfer.response.IServletResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.pojo.domain.Inventory;
import com.dev.servlet.domain.model.pojo.enums.RequestMethod;
import com.dev.servlet.domain.service.CategoryService;
import com.dev.servlet.domain.service.InventoryService;
import com.dev.servlet.presentation.controller.base.BaseController;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

@NoArgsConstructor
@Singleton
@Controller("inventory")
public class InventoryController extends BaseController<Inventory, Long> {

    private InventoryService inventoryService;
    private CategoryService categoryService;

    @Inject
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Inject
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Forward to the register form.
     *
     * @return the response {@linkplain IHttpResponse} with the next path
     */
    @RequestMapping(value = "/new")
    public IHttpResponse<Void> forwardRegister() {
        return HttpResponse.<Void>ok().next(forwardTo("formCreateItem")).build();
    }

    /**
     * Create a new item.
     *
     * @param request {@linkplain Request}
     * @return the response {@linkplain IHttpResponse} with the next path
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "description", constraints = {
                            @Constraints(minLength = 5, message = "Description must be between {0} and {1} characters")
                    }),
                    @Validator(values = "quantity", constraints = {
                            @Constraints(min = 1, message = "Quantity must be greater than or equal to {0}")
                    }),
                    @Validator(values = "productId", constraints = {
                            @Constraints(min = 1, message = "Product ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> create(Request request) throws ServiceException {
        InventoryDTO inventory = inventoryService.create(request);
        // Created
        return newHttpResponse(201, redirectTo(inventory.getId()));
    }

    /**
     * Delete an item.
     *
     * @param request {@linkplain Request}
     * @return the response {@linkplain IHttpResponse} with the next path
     */
    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> delete(Request request) {
        inventoryService.delete(request);
        return HttpResponse.<Void>ok().next(redirectTo("list")).build();
    }

    /**
     * List all items.
     *
     * @param request {@linkplain Request}
     * @return the response {@linkplain IServletResponse} with the next path
     */
    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {
        Collection<InventoryDTO> inventories = inventoryService.list(request);
        Collection<CategoryDTO> categories = categoryService.list(request);

        Set<KeyPair> data = Set.of(
                new KeyPair("items", inventories),
                new KeyPair("categories", categories)
        );

        return newServletResponse(data, forwardTo("listItems"));
    }

    /**
     * List an item by ID.
     *
     * @param request {@linkplain Request}
     * @return the response {@linkplain IServletResponse} with the next path
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(
                            values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<InventoryDTO> listById(Request request) throws ServiceException {
        InventoryDTO inventory = inventoryService.listById(request);
        // OK
        return okHttpResponse(inventory, forwardTo("formListItem"));
    }

    /**
     * Edit an item.
     *
     * @param request {@linkplain Request}
     * @return the response {@linkplain IServletResponse} with the next path
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/edit/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<InventoryDTO> edit(Request request) throws ServiceException {
        InventoryDTO inventory = inventoryService.listById(request);
        // OK
        return okHttpResponse(inventory, forwardTo("formUpdateItem"));
    }

    /**
     * Update an item.
     *
     * @param request {@linkplain Request}
     * @param model   {@linkplain InventoryService}
     * @return the response {@linkplain IServletResponse} with the next path
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/update/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than {0}")
                    }),
                    @Validator(values = "description", constraints = {
                            @Constraints(minLength = 5, message = "Description must be between {0} and {1} characters")
                    }),
                    @Validator(values = "quantity", constraints = {
                            @Constraints(min = 1, message = "Quantity must be greater than or equal to {0}")
                    }),
                    @Validator(values = "productId", constraints = {
                            @Constraints(min = 1, message = "Product ID must be greater than {0}")
                    })
            })
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        InventoryDTO inventory = inventoryService.update(request);
        // No content
        return newHttpResponse(204, redirectTo(inventory.getId()));
    }
}