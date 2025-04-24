package com.dev.servlet.controller;
import com.dev.servlet.domain.transfer.dto.CategoryDTO;
import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.transfer.records.KeyPair;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.domain.transfer.response.IServletResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.Property;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.enums.RequestMethod;
import com.dev.servlet.domain.service.ICategoryService;
import com.dev.servlet.domain.service.IProductService;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import com.dev.servlet.controller.base.BaseController;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
@Slf4j
@Singleton
@Controller("product")
public class ProductController extends BaseController {
    @Inject
    @Named("productServiceProxy")
    private IProductService productService;
    @Inject
    private ICategoryService categoryService;

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
        return newHttpResponse(201, redirectTo(product.getId()));
    }

    @RequestMapping(value = "/new")
    public IHttpResponse<Collection<CategoryDTO>> forward(Request request) {
        var categories = categoryService.list(request.withToken());
        return newHttpResponse(302, categories, forwardTo("formCreateProduct"));
    }

    @RequestMapping(
            value = "/edit/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IServletResponse edit(Request request) {
        ProductDTO product = productService.findById(request);
        Collection<CategoryDTO> categories = categoryService.list(request.withToken());
        Set<KeyPair> data = Set.of(
                new KeyPair("product", product),
                new KeyPair("categories", categories)
        );
        return newServletResponse(data, forwardTo("formUpdateProduct"));
    }

    @RequestMapping(value = "/list")
    public IServletResponse list(Request request) {
        Product filter = productService.getEntity(request);
        IPageable<ProductDTO> page = getAllPageable(request.getQuery().getPageRequest(), filter);
        BigDecimal price = calculateTotalPrice(page, filter);
        Collection<CategoryDTO> categories = categoryService.list(request.withToken());
        Set<KeyPair> container = new HashSet<>();
        container.add(new KeyPair("pageable", page));
        container.add(new KeyPair("totalPrice", price));
        container.add(new KeyPair("categories", categories));
        return newServletResponse(container, forwardTo("listProducts"));
    }

    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<ProductDTO> getById(Request request) {
        ProductDTO product = productService.findById(request);
        return okHttpResponse(product, forwardTo("formListProduct"));
    }

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
    @SneakyThrows
    public IHttpResponse<Void> update(Request request) {
        ProductDTO product = productService.update(request);
        return newHttpResponse(204, redirectTo(product.getId()));
    }

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
    @SneakyThrows
    public IHttpResponse<Void> delete(Request request) {
        productService.delete(request);
        return HttpResponse.<Void>next(redirectTo(LIST)).build();
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

    @SneakyThrows
    @RequestMapping(value = "/scrape", method = RequestMethod.GET)
    public IHttpResponse<Void> scrape(Request request,
                                      @Property("env") String environment,
                                      @Property("scrape.product.url") String url) {
        if (!"development".equals(environment)) {
            log.warn("Web scraping is only allowed in development environment");
            return HttpResponse.<Void>next(redirectTo(LIST)).build();
        }
        Optional<List<ProductDTO>> response = productService.scrape(request, url);
        return HttpResponse.<Void>next(redirectTo(LIST)).build();
    }
}
