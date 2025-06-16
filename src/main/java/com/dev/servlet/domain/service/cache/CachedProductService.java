package com.dev.servlet.domain.service.cache;

import com.dev.servlet.application.dto.ProductDTO;
import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.core.decorator.CachedServiceDecorator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.domain.model.pojo.domain.Product;
import com.dev.servlet.domain.service.ProductService;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A ProductService implementation that uses a CachedServiceDecorator internally.
 * This class delegates all method calls to the decorated service while applying caching per user.
 *
 * @author marcelo.feliciano
 * @since 2.0
 */
@NoArgsConstructor
public class CachedProductService {

    private static final ConcurrentMap<String, CachedServiceDecorator<Product, Long>> userCacheDecorators = new ConcurrentHashMap<>();
    private ProductService delegateService;

    @Inject
    public CachedProductService(ProductService productService) {
        this.delegateService = productService;
    }

    /**
     * Gets a cached decorator for the user based on their authentication token.
     *
     * @param token The user's authentication token
     * @return A cached decorator for the user
     */
    protected CachedServiceDecorator<Product, Long> getDecoratorForToken(String token) {
        return userCacheDecorators.computeIfAbsent(token,
                key -> new CachedServiceDecorator<>(delegateService, "product", key));
    }

    // Override methods that need request context to extract the token
    public ProductDTO create(Request request) {
        ProductDTO result = delegateService.create(request);
        CachedServiceDecorator<Product, Long> decorator = getDecoratorForToken(request.token());
        decorator.invalidateCache();
        return result;
    }

    public ProductDTO update(Request request) throws ServiceException {
        ProductDTO result = delegateService.update(request);
        CachedServiceDecorator<Product, Long> decorator = getDecoratorForToken(request.token());
        decorator.invalidateCache();
        return result;
    }

    public void delete(Request request) throws ServiceException {
        delegateService.delete(request);
        CachedServiceDecorator<Product, Long> decorator = getDecoratorForToken(request.token());
        decorator.invalidateCache();
    }

    // Methods that need the token passed explicitly
    public Collection<Long> findAll(Request request) {
        CachedServiceDecorator<Product, Long> decorator = getDecoratorForToken(request.token());

        Product filter = delegateService.getEntity(request);
        if (filter != null) {
            return decorator.findAll(filter).stream().map(Product::getId).toList();
        }

        return delegateService.findAll(request);
    }

    public ProductDTO getById(Request request) throws ServiceException {
        CachedServiceDecorator<Product, Long> decorator = getDecoratorForToken(request.token());

        Product filter = delegateService.getEntity(request);
        if (filter != null && filter.getId() != null) {
            Product product = decorator.findById(filter.getId());
            if (product != null) {
                return ProductMapper.full(product);
            }
        }

        return delegateService.getById(request);
    }

    /**
     * Delegates the call to getAllPageable, applying the token context for caching.
     */
    public <U> IPageable<U> getAllPageable(IPageRequest<Product> pageRequest, Mapper<Product, U> mapper) {
        String token = pageRequest.getFilter().getUser().getToken();
        CachedServiceDecorator<Product, Long> decorator = getDecoratorForToken(token);
        return decorator.getAllPageable(pageRequest, mapper);
    }

    // Fallback to standard implementations for methods without token context
    public List<Product> saveAll(List<Product> products, String token) throws ServiceException {
        products = delegateService.saveAll(products);
        clearCache(token);
        return products;
    }

    public BigDecimal calculateTotalPriceFor(Product filter) {
        return delegateService.calculateTotalPriceFor(filter);
    }

    /**
     * Clears cache for a specific user token
     *
     * @param token The user's authentication token
     */
    public void clearCache(String token) {
        if (token != null) {
            userCacheDecorators.remove(token);
        }
        delegateService.clearCache(token);
    }

    public Product getEntity(Request request) {
        return delegateService.getEntity(request);
    }
}