package com.dev.servlet.controller;
import com.dev.servlet.domain.transfer.dto.CategoryDTO;
import com.dev.servlet.domain.transfer.dto.InventoryDTO;
import com.dev.servlet.domain.transfer.records.KeyPair;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.domain.transfer.response.IServletResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.enums.RequestMethod;
import com.dev.servlet.domain.service.ICategoryService;
import com.dev.servlet.domain.service.IStockService;
import com.dev.servlet.controller.base.BaseController;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

@NoArgsConstructor
@Singleton
@Controller("inventory")
public class InventoryController extends BaseController {
    @Inject
    private IStockService stockService;
    @Inject
    private ICategoryService categoryService;

    @RequestMapping(value = "/new")
    public IHttpResponse<Void> forwardRegister() {
        return HttpResponse.<Void>next(forwardTo("formCreateItem")).build();
    }

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
        InventoryDTO inventory = stockService.create(request);
        return newHttpResponse(201, redirectTo(inventory.getId()));
    }

    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<Void> delete(Request request) {
        stockService.delete(request);
        return HttpResponse.<Void>next(redirectTo("list")).build();
    }

    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {
        Collection<InventoryDTO> inventories = stockService.list(request);
        Collection<CategoryDTO> categories = categoryService.list(request);
        Set<KeyPair> data = Set.of(
                new KeyPair("items", inventories),
                new KeyPair("categories", categories)
        );
        return newServletResponse(data, forwardTo("listItems"));
    }

    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(
                            values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<InventoryDTO> findById(Request request) throws ServiceException {
        InventoryDTO inventory = stockService.findById(request);
        return okHttpResponse(inventory, forwardTo("formListItem"));
    }

    @RequestMapping(
            value = "/edit/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<InventoryDTO> edit(Request request) throws ServiceException {
        InventoryDTO inventory = stockService.findById(request);
        return okHttpResponse(inventory, forwardTo("formUpdateItem"));
    }

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
        InventoryDTO inventory = stockService.update(request);
        return newHttpResponse(204, redirectTo(inventory.getId()));
    }
}
