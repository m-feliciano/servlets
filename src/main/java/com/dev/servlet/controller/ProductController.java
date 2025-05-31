package com.dev.servlet.controller;

import com.dev.servlet.controller.base.BaseController;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.IServletResponse;
import com.dev.servlet.core.RequestMapping;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.model.impl.ProductModel;
import com.dev.servlet.model.pojo.domain.Product;
import com.dev.servlet.model.pojo.enums.RequestMethod;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.KeyPair;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.persistence.IPageRequest;
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
        return super.newHttpResponse(201, redirectTo(product.getId()));
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
        return super.newHttpResponse(302, categories, forwardTo("formCreateProduct"));
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

        return super.newServletResponse(data, forwardTo("formUpdateProduct"));
    }

    /**
     * List all products
     *
     * @param request {@linkplain Request} with query
     * @return {@linkplain IServletResponse} with {@linkplain ProductDTO}
     */
    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {
        Product filter = getModel().getEntity(request);

        IPageable<ProductDTO> page = getProductDTOPage(request.query().getPageRequest(), filter);
        BigDecimal price = calculateTotalPrice(page, filter);
        Collection<CategoryDTO> categories = categoryController.list(request.withToken()).body();

        Set<KeyPair> container = new HashSet<>();
        container.add(new KeyPair("pageable", page));
        container.add(new KeyPair("totalPrice", price));
        container.add(new KeyPair("categories", categories));

        return super.newServletResponse(container, forwardTo("listProducts"));
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
        return super.okHttpResponse(product, forwardTo("formListProduct"));
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
        return super.newHttpResponse(204, redirectTo(product.getId()));
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

        return HttpResponseImpl.<Void>ok().next(redirectTo(LIST)).build();
    }

    private IPageable<ProductDTO> getProductDTOPage(IPageRequest<Product> pageRequest, Product filter) {
        pageRequest.setFilter(filter);

        return baseModel.getAllPageable(pageRequest, ProductMapper::base);
    }

    private BigDecimal calculateTotalPrice(IPageable<?> page, Product filter) {
        if (page != null && page.getContent().iterator().hasNext()) {
            ProductModel model = getModel();
            return model.calculateTotalPriceFor(filter);
        }

        return BigDecimal.ZERO;
    }
}
