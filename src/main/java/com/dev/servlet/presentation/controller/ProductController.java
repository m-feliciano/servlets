package com.dev.servlet.presentation.controller;

import com.dev.servlet.application.transfer.dto.CategoryDTO;
import com.dev.servlet.application.transfer.dto.ProductDTO;
import com.dev.servlet.application.transfer.records.KeyPair;
import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.application.transfer.response.IHttpResponse;
import com.dev.servlet.application.transfer.response.IServletResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.Property;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.domain.model.pojo.domain.Product;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.model.pojo.enums.RequestMethod;
import com.dev.servlet.domain.model.pojo.enums.Status;
import com.dev.servlet.domain.service.CategoryService;
import com.dev.servlet.domain.service.cache.CachedProductService;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeRequest;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeService;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeServiceRegistry;
import com.dev.servlet.infrastructure.external.webscrape.transfer.ProductWebScrapeDTO;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import com.dev.servlet.presentation.controller.base.BaseController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.dev.servlet.core.util.CryptoUtils.getUser;

@NoArgsConstructor
@Slf4j
@Singleton
@Controller("product")
public class ProductController extends BaseController {

    private CachedProductService productService;
    private CategoryService categoryService;
    private WebScrapeServiceRegistry webScrapeServiceRegistry;

    private static Product prepareProductToSave(Product product, User user) {
        Date now = new Date();
        product.setRegisterDate(now);
        product.setStatus(Status.ACTIVE.getValue());
        product.setUser(user);
        return product;
    }

    @Inject
    public void setProductService(CachedProductService productService) {
        this.productService = productService;
    }

    @Inject
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Inject
    public void setWebScrapeServiceRegistry(WebScrapeServiceRegistry webScrapeServiceRegistry) {
        this.webScrapeServiceRegistry = webScrapeServiceRegistry;
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
        ProductDTO product = productService.create(request);
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
    public IHttpResponse<Collection<CategoryDTO>> forward(Request request) {
        var categories = categoryService.list(request.withToken());
        // Found
        return newHttpResponse(302, categories, forwardTo("formCreateProduct"));
    }

    /**
     * Load data and forward to the edit product form
     *
     * @param request        {@linkplain Request}
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

        ProductDTO product = productService.getById(request);
        Collection<CategoryDTO> categories = categoryService.list(request.withToken());

        Set<KeyPair> data = Set.of(
                new KeyPair("product", product),
                new KeyPair("categories", categories)
        );

        return newServletResponse(data, forwardTo("formUpdateProduct"));
    }

    /**
     * List all products
     *
     * @param request        {@linkplain Request} with query
     * @return {@linkplain IServletResponse} with {@linkplain ProductDTO}
     */
    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {

        Product filter = productService.getEntity(request);

        IPageable<ProductDTO> page = getAllPageable(request.query().getPageRequest(), filter);
        BigDecimal price = calculateTotalPrice(page, filter);

        Collection<CategoryDTO> categories = categoryService.list(request.withToken());

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
    public IHttpResponse<ProductDTO> getById(Request request) throws ServiceException {
        ProductDTO product = productService.getById(request);
        // OK
        return okHttpResponse(product, forwardTo("formListProduct"));
    }

    /**
     * Update a product
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponse}
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
                            @Constraints(minLength = 5, maxLength = 1024, message = "Description must be between {0} and {1} characters")
                    }),
                    @Validator(values = "price", constraints = {
                            @Constraints(min = 0, message = "Price must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        ProductDTO product = productService.update(request);
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
    public IHttpResponse<Void> delete(Request request) throws ServiceException {
        productService.delete(request);
        return HttpResponse.<Void>ok().next(redirectTo(LIST)).build();
    }

    private IPageable<ProductDTO> getAllPageable(IPageRequest<Product> pageRequest, Product filter) {
        pageRequest.setFilter(filter);

        return productService.getAllPageable(pageRequest, ProductMapper::base);
    }

    private BigDecimal calculateTotalPrice(IPageable<?> page, Product filter) {
        if (page != null && page.getContent().iterator().hasNext()) {
            return productService.calculateTotalPriceFor(filter);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Scrape product data from a web page
     *
     * @param request {@linkplain Request}
     * @param url     the URL to scrape
     * @return {@linkplain IHttpResponse} with no results {@linkplain Void}
     * @throws Exception if any error occurs
     */
    @RequestMapping(value = "/scrape", method = RequestMethod.GET)
    public IHttpResponse<Void> scrape(Request request,
                                      @Property("env") String environment,
                                      @Property("scrape.product.url") String url) throws Exception {

        if (!"development".equals(environment)) {
            log.warn("Web scraping is only allowed in development environment");
            return HttpResponse.<Void>ok().next(redirectTo(LIST)).build();
        }

        var webScrapeRequest = new WebScrapeRequest("product", url, null);
        var webScrapeService = new WebScrapeService<List<ProductWebScrapeDTO>>(webScrapeServiceRegistry);

        Optional<List<ProductWebScrapeDTO>> response = webScrapeService.run(webScrapeRequest);
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
                    products = productService.saveAll(products, request.token());
                    Objects.requireNonNull(products);
                } catch (ServiceException e) {
                    log.error("Error saving scraped products: {}", e.getMessage(), e);
                }
            }
        });

        return HttpResponse.<Void>ok().next(redirectTo(LIST)).build();
    }
}
