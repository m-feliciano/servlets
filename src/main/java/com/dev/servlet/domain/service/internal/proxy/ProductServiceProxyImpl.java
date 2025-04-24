package com.dev.servlet.domain.service.internal.proxy;
import com.dev.servlet.domain.transfer.dto.DataTransferObject;
import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.cache.CachedServiceDecorator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.service.IProductService;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Setter
@RequiredArgsConstructor
@Named("productServiceProxy")
public class ProductServiceProxyImpl implements IProductService {
    @Inject
    @Named("productService")
    private IProductService delegateService;
    private CachedServiceDecorator<Product, Long> createCachedService(String authorization) {
        return new CachedServiceDecorator<>(delegateService, "product", authorization);
    }
    @Override
    public ProductDTO create(Request request) {
        ProductDTO result = delegateService.create(request);
        CachedServiceDecorator<Product, Long> serviceDecorator = createCachedService(request.getToken());
        serviceDecorator.invalidateCache();
        return result;
    }
    @Override
    public ProductDTO update(Request request) throws ServiceException {
        ProductDTO result = delegateService.update(request);
        CachedServiceDecorator<Product, Long> serviceDecorator = createCachedService(request.getToken());
        serviceDecorator.invalidateCache();
        return result;
    }
    @Override
    public boolean delete(Request request) throws ServiceException {
        boolean deleted = delegateService.delete(request);
        CachedServiceDecorator<Product, Long> serviceDecorator = createCachedService(request.getToken());
        serviceDecorator.invalidateCache();
        return deleted;
    }
    @Override
    public IPageable<Product> getAllPageable(IPageRequest<Product> pageRequest) {
        String token = pageRequest.getFilter().getUser().getToken();
        CachedServiceDecorator<Product, Long> serviceDecorator = createCachedService(token);
        return serviceDecorator.getAllPageable(pageRequest);
    }
    @Override
    public <U> IPageable<U> getAllPageable(IPageRequest<Product> pageRequest, Mapper<Product, U> mapper) {
        String token = pageRequest.getFilter().getUser().getToken();
        CachedServiceDecorator<Product, Long> serviceDecorator = createCachedService(token);
        return serviceDecorator.getAllPageable(pageRequest, mapper);
    }
    @Override
    public List<Product> save(List<Product> products, String authorization) throws ServiceException {
        products = delegateService.save(products, authorization);
        CachedServiceDecorator<Product, Long> serviceDecorator = createCachedService(authorization);
        serviceDecorator.invalidateCache();
        return products;
    }
    @Override
    public ProductDTO findById(Request request) throws ServiceException {
        Product filter = delegateService.getEntity(request);
        if (filter != null && filter.getId() != null) {
            var decorator = createCachedService(request.getToken());
            Product product = decorator.findById(filter.getId());
            if (product != null) {
                return ProductMapper.full(product);
            }
        }
        return delegateService.findById(request);
    }
    @Override
    public Product findById(Long aLong) {
        return delegateService.findById(aLong);
    }
    @Override
    public Product find(Product filter) {
        return delegateService.find(filter);
    }
    @Override
    public Collection<Product> findAll(Product object) {
        return delegateService.findAll(object);
    }
    @Override
    public Product save(Product object) {
        return delegateService.save(object);
    }
    @Override
    public Product update(Product object) {
        return delegateService.update(object);
    }
    @Override
    public boolean delete(Product object) {
        return delegateService.delete(object);
    }
    @Override
    public BigDecimal calculateTotalPriceFor(Product product) {
        return delegateService.calculateTotalPriceFor(product);
    }
    @Override
    public Optional<List<ProductDTO>> scrape(Request request, String url) {
        return delegateService.scrape(request, url);
    }
    @Override
    public Product getEntity(Request request) {
        return delegateService.getEntity(request);
    }
    @Override
    public Product toEntity(Object object) {
        return delegateService.toEntity(object);
    }
    @Override
    public Class<? extends DataTransferObject<Long>> getDataMapper() {
        return delegateService.getDataMapper();
    }
}
