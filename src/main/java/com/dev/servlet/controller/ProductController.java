package com.dev.servlet.controller;

import com.dev.servlet.controller.base.BaseController;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.IServletResponse;
import com.dev.servlet.core.RequestMapping;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.impl.ProductModel;
import com.dev.servlet.model.pojo.domain.Product;
import com.dev.servlet.model.pojo.enums.RequestMethod;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.KeyPair;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.persistence.IPageable;
import com.dev.servlet.validator.Constraints;
import com.dev.servlet.validator.Validator;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Controller(path = "/product")
public final class ProductController extends BaseController<Product, Long> {

    private CategoryController categoryController;

    @Inject
    public ProductController(ProductModel productModel) {
        super(productModel);
    }

    @Inject
    public void setCategoryController(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

    private ProductModel getModel() {
        return (ProductModel) super.getBaseModel();
    }

    /**
     * Create a new product
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "name", constraints = {
                            @Constraints(minLength = 3, maxLength = 50, message = "Name must be between {0} and {1} characters")
                    }),
                    @Validator(values = "description", constraints = {
                            @Constraints(minLength = 5, maxLength = 255, message = "Description must be between {0} and {1} characters")
                    }),
                    @Validator(values = "price", constraints = {
                            @Constraints(min = 0, message = "Price must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> create(Request request) {
        ProductModel model = this.getModel();
        ProductDTO product = model.create(request);
        // Created
        String next = super.redirectTo(product.getId());
        return super.newHttpResponse(201, next);
    }

    /**
     * Load data and forward to the create product form
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
    @RequestMapping(value = "/new")
    public IHttpResponse<Collection<CategoryDTO>> forward(Request request) {
        var categories = categoryController.list(request.withToken()).body();
        // Found
        return super.newHttpResponse(302, categories, super.forwardTo("formCreateProduct"));
    }

    /**
     * Load data and forward to the edit product form
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IServletResponse}
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/edit/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IServletResponse edit(Request request) throws ServiceException {
        ProductModel model = this.getModel();
        ProductDTO product = model.getById(request);
        Collection<CategoryDTO> categories = categoryController.list(request.withToken()).body();

        Set<KeyPair> data = Set.of(
                new KeyPair("product", product),
                new KeyPair("categories", categories)
        );

        return super.newServletResponse(data, super.forwardTo("formUpdateProduct"));
    }

    /**
     * List all products
     *
     * @param request {@linkplain Request} with query
     * @return {@linkplain IServletResponse} with {@linkplain ProductDTO}
     */
    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {
        ProductModel model = this.getModel();
        Product filter = model.getEntity(request);

        request.query().getPageRequest().setFilter(filter);

        IPageable<Product> pageable = model.getAllPageable(request.query().getPageRequest());

        Set<KeyPair> container = new HashSet<>();
        container.add(new KeyPair("pageable", pageable));

        if (pageable.getContent().iterator().hasNext()) {
            BigDecimal totalPrice = model.calculateTotalPriceFor(filter);
            container.add(new KeyPair("totalPrice", totalPrice));
        }

        Collection<CategoryDTO> categories = categoryController.list(request.withToken()).body();

        container.add(new KeyPair("categories", categories));

        String next = super.forwardTo("listProducts");
        return super.newServletResponse(container, next);
    }

    /**
     * List product by id
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with {@linkplain ProductDTO}
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<ProductDTO> listById(Request request) throws ServiceException {
        ProductModel model = this.getModel();
        ProductDTO product = model.getById(request);
        // OK
        return super.okHttpResponse(product, super.forwardTo("formListProduct"));
    }

    /**
     * Update a product
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/update/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    }),
                    @Validator(values = "name", constraints = {
                            @Constraints(minLength = 3, maxLength = 50, message = "Name must be between {0} and {1} characters")
                    }),
                    @Validator(values = "description", constraints = {
                            @Constraints(minLength = 5, maxLength = 255, message = "Description must be between {0} and {1} characters")
                    }),
                    @Validator(values = "price", constraints = {
                            @Constraints(min = 0, message = "Price must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        ProductModel model = this.getModel();
        ProductDTO product = model.update(request);
        // No Content
        String next = super.redirectTo(product.getId());
        return super.newHttpResponse(204, next);
    }

    /**
     * Delete a product
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with no content {@linkplain Void}
     * @throws ServiceException if any error occurs
     */
    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(
                                    min = 1,
                                    message = "ID must be greater than or equal to {0}"
                            )
                    })
            })
    public IHttpResponse<Void> delete(Request request) throws ServiceException {
        ProductModel model = this.getModel();
        model.delete(request);

        String next = super.redirectTo(LIST);
        return HttpResponseImpl.<Void>ok().next(next).build();
    }
}
