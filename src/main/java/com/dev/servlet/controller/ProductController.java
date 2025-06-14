package com.dev.servlet.controller;

import com.dev.servlet.adapter.IHttpResponse;
import com.dev.servlet.adapter.IServletResponse;
import com.dev.servlet.adapter.Property;
import com.dev.servlet.adapter.RequestMapping;
import com.dev.servlet.controller.base.BaseController;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.model.impl.CategoryModel;
import com.dev.servlet.model.impl.ProductModel;
import com.dev.servlet.model.pojo.domain.Product;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.enums.RequestMethod;
import com.dev.servlet.model.pojo.enums.Status;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.KeyPair;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.persistence.IPageRequest;
import com.dev.servlet.persistence.IPageable;
import com.dev.servlet.validator.Constraints;
import com.dev.servlet.validator.Validator;
import com.dev.servlet.web.dto.ProductWebScrapeDTO;
import com.dev.servlet.web.scrape.WebScrapeServiceEnum;
import com.dev.servlet.web.service.WebScrapeRequest;
import com.dev.servlet.web.service.WebScrapeService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.dev.servlet.util.CryptoUtils.getUser;

@Slf4j
@NoArgsConstructor
@Controller(path = "/product")
public final class ProductController extends BaseController<Product, Long> {

    /**
     * Create a new product
     *
     * @param request {@linkplain Request}
     * @param model   {@linkplain ProductModel}
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
    public IHttpResponse<Void> create(Request request, ProductModel model) {
        ProductDTO product = model.create(request);
        // Created
        return newHttpResponse(201, redirectTo(product.getId()));
    }

    /**
     * Load data and forward to the create product form
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
    @RequestMapping(value = "/new")
    public IHttpResponse<Collection<CategoryDTO>> forward(Request request, CategoryModel categoryModel) {
        var categories = categoryModel.list(request.withToken());
        // Found
        return newHttpResponse(302, categories, forwardTo("formCreateProduct"));
    }

    /**
     * Load data and forward to the edit product form
     *
     * @param request {@linkplain Request}
     * @param productModel   {@linkplain ProductModel}
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
    public IServletResponse edit(Request request, ProductModel productModel, CategoryModel categoryModel) throws ServiceException {
        ProductDTO product = productModel.getById(request);
        Collection<CategoryDTO> categories = categoryModel.list(request.withToken());

        Set<KeyPair> data = Set.of(
                new KeyPair("product", product),
                new KeyPair("categories", categories)
        );

        return newServletResponse(data, forwardTo("formUpdateProduct"));
    }

    /**
     * List all products
     *
     * @param request {@linkplain Request} with query
     * @param productModel   {@linkplain ProductModel}
     * @return {@linkplain IServletResponse} with {@linkplain ProductDTO}
     */
    @RequestMapping(value = "/list")
    public IServletResponse list(Request request, ProductModel productModel, CategoryModel categoryModel) {

        Product filter = productModel.getEntity(request);

        IPageable<ProductDTO> page =
                getAllPageable(request.query().getPageRequest(), filter, productModel);
        BigDecimal price = calculateTotalPrice(page, filter, productModel);

        Collection<CategoryDTO> categories = categoryModel.list(request.withToken());

        Set<KeyPair> container = new HashSet<>();
        container.add(new KeyPair("pageable", page));
        container.add(new KeyPair("totalPrice", price));
        container.add(new KeyPair("categories", categories));

        return newServletResponse(container, forwardTo("listProducts"));
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
    public IHttpResponse<ProductDTO> getById(Request request, ProductModel model) throws ServiceException {
        ProductDTO product = model.getById(request);
        // OK
        return okHttpResponse(product, forwardTo("formListProduct"));
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
                            @Constraints(minLength = 5, message = "Description must be at least {0} characters"),
                    }),
                    @Validator(values = "price", constraints = {
                            @Constraints(min = 0, message = "Price must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> update(Request request, ProductModel model) throws ServiceException {
        ProductDTO product = model.update(request);
        // No Content
        return newHttpResponse(204, redirectTo(product.getId()));
    }

    /**
     * Delete a product
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with no results {@linkplain Void}
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
    public IHttpResponse<Void> delete(Request request, ProductModel model) throws ServiceException {
        model.delete(request);
        return HttpResponseImpl.<Void>ok().next(redirectTo(LIST)).build();
    }

    private IPageable<ProductDTO> getAllPageable(IPageRequest<Product> pageRequest, Product filter, ProductModel model) {
        pageRequest.setFilter(filter);

        return model.getAllPageable(pageRequest, ProductMapper::base);
    }

    private BigDecimal calculateTotalPrice(IPageable<?> page, Product filter, ProductModel model) {
        if (page != null && page.getContent().iterator().hasNext()) {
            return model.calculateTotalPriceFor(filter);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Scrape product data from a web page
     *
     * @param request {@linkplain Request}
     * @param model   {@linkplain ProductModel}
     * @param url     the URL to scrape
     * @return {@linkplain IHttpResponse} with no results {@linkplain Void}
     * @throws Exception if any error occurs
     */
    @RequestMapping(value = "/scrape", method = RequestMethod.GET)
    public IHttpResponse<Void> scrape(Request request,
                                      ProductModel model,
                                      @Property("scrape.product.url") String url) throws Exception {

        var webScrapeRequest = new WebScrapeRequest(WebScrapeServiceEnum.PRODUCT, url, null);
        var webScrapeService = new WebScrapeService<List<ProductWebScrapeDTO>>(webScrapeRequest);

        Optional<List<ProductWebScrapeDTO>> response = webScrapeService.run();
        response.ifPresent((res) -> {
            if (res.isEmpty()) {
                log.warn("No products found in the web scrape response");
            } else {
                log.info("Web scrape returned {} products", res.size());

                User user = getUser(request.token());

                List<Product> products = res.stream()
                        .map(ProductMapper::fromWebScrapeDTO)
                        .map(product -> prepareProductToSave(product, user))
                        .toList();
                try {
                    products = model.saveAll(products);
                } catch (ServiceException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return HttpResponseImpl.<Void>ok().next(redirectTo(LIST)).build();
    }

    private static Product prepareProductToSave(Product product, User user) {
        Date now = new Date();
        product.setRegisterDate(now);
        product.setStatus(Status.ACTIVE.getValue());
        product.setUser(user);
        return product;
    }
}