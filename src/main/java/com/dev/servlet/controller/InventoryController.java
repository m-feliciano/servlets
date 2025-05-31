package com.dev.servlet.controller;

import com.dev.servlet.controller.base.BaseController;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.IServletResponse;
import com.dev.servlet.core.RequestMapping;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.impl.InventoryModel;
import com.dev.servlet.model.pojo.domain.Inventory;
import com.dev.servlet.model.pojo.enums.RequestMethod;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.KeyPair;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.validator.Constraints;
import com.dev.servlet.validator.Validator;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;


@NoArgsConstructor
@Controller(path = "/inventory")
public final class InventoryController extends BaseController<Inventory, Long> {

    CategoryController categoryController;

    @Inject
    public InventoryController(InventoryModel inventoryModel) {
        super(inventoryModel);
    }

    @Inject
    public void setCategoryController(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

    private InventoryModel getModel() {
        return (InventoryModel) super.getBaseModel();
    }

    /**
     * Forward to the register form.
     *
     * @return the response {@linkplain IHttpResponse} with the next path
     */
    @RequestMapping(value = "/new")
    public IHttpResponse<Void> forwardRegister() {
        return HttpResponseImpl.<Void>ok().next(forwardTo("formCreateItem")).build();
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
        InventoryModel model = this.getModel();
        InventoryDTO inventory = model.create(request);
        // Created
        return super.newHttpResponse(201, redirectTo(inventory.getId()));
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
        InventoryModel model = this.getModel();
        model.delete(request);

        return HttpResponseImpl.<Void>ok().next(redirectTo("list")).build();
    }

    /**
     * List all items.
     *
     * @param request {@linkplain Request}
     * @return the response {@linkplain IServletResponse} with the next path
     */
    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {
        Collection<InventoryDTO> inventories = this.getModel().list(request);
        Collection<CategoryDTO> categories = categoryController.list(request).body();

        Set<KeyPair> data = Set.of(
                new KeyPair("items", inventories),
                new KeyPair("categories", categories)
        );

        return super.newServletResponse(data, forwardTo("listItems"));
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
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<InventoryDTO> listById(Request request) throws ServiceException {
        InventoryModel model = this.getModel();
        InventoryDTO inventory = model.listById(request);
        // OK
        return super.okHttpResponse(inventory, forwardTo("formListItem"));
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
        InventoryModel model = this.getModel();
        InventoryDTO inventory = model.listById(request);
        // OK
        return super.okHttpResponse(inventory, forwardTo("formUpdateItem"));
    }

    /**
     * Update an item.
     *
     * @param request {@linkplain Request}
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
        InventoryModel model = this.getModel();
        InventoryDTO inventory = model.update(request);
        // No content
        return super.newHttpResponse(204, redirectTo(inventory.getId()));
    }
}
