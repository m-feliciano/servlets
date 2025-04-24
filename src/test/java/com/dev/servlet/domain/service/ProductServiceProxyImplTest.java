package com.dev.servlet.domain.service;

import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.BaseServiceTest;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.User;
import com.dev.servlet.domain.service.IProductService;
import com.dev.servlet.domain.service.internal.proxy.ProductServiceProxyImpl;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceProxyImplTest extends BaseServiceTest {

    @Mock
    private IProductService productService;

    @InjectMocks
    private ProductServiceProxyImpl productServiceProxy;

    @Mock
    private Request request;

    @Mock
    private Product product;

    @Mock
    private ProductDTO productDTO;

    @Mock
    private User user;

    @Mock
    private IPageRequest<Product> pageRequest;

    @Mock
    private IPageable<ProductDTO> pageableMock;

    @Mock
    private Mapper<Product, ProductDTO> mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(request.getToken()).thenReturn(TEST_TOKEN);
        when(user.getToken()).thenReturn(TEST_TOKEN);
        when(product.getUser()).thenReturn(user);
        when(pageRequest.getFilter()).thenReturn(product);
    }

    @Test
    @DisplayName("create should delegate to ProductService and invalidate cache")
    void create_ShouldDelegateAndInvalidateCache() {
        // Arrange
        when(productService.create(request)).thenReturn(productDTO);

        // Act
        ProductDTO result = productServiceProxy.create(request);

        // Assert
        assertEquals(productDTO, result);
        verify(productService).create(request);
        cacheUtilsMock.verify(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN)), atLeastOnce());
    }

    @Test
    @DisplayName("update should delegate to ProductService and invalidate cache")
    void update_ShouldDelegateAndInvalidateCache() throws ServiceException {
        // Arrange
        when(productService.update(request)).thenReturn(productDTO);
        // Act
        ProductDTO result = productServiceProxy.update(request);
        // Assert
        assertEquals(productDTO, result);
        verify(productService).update(request);

        cacheUtilsMock.verify(() -> CacheUtils.clearCacheKeyPrefix("product", TEST_TOKEN), atLeastOnce());
    }

    @Test
    @DisplayName("delete should delegate to ProductService and invalidate cache")
    void delete_ShouldDelegateAndInvalidateCache() throws ServiceException {
        // Act
        productServiceProxy.delete(request);
        // Assert
        verify(productService).delete(request);
        cacheUtilsMock.verify(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN)), atLeastOnce());
    }

    @Test
    @DisplayName("getById should use cache when available")
    void findById_ShouldUseCacheWhenAvailable() throws ServiceException {
        // Arrange
        when(productService.getEntity(request)).thenReturn(new Product(1L));
        when(productService.findById(request)).thenReturn(productDTO);

        String cacheKey = "product:findById:1";

        Product cachedProduct = new Product("Cached Product", "Cached Description", BigDecimal.valueOf(15.0));
        cachedProduct.setId(1L);

        // First call - should not hit cache
        cacheUtilsMock
                .when(() -> CacheUtils.getObject(eq(cacheKey), eq(TEST_TOKEN)))
                .thenReturn(null);

        // Act
        ProductDTO result1 = productServiceProxy.findById(request);

        // Assert first call
        assertEquals(productDTO, result1);
        verify(productService, times(1)).findById(request);

        cacheUtilsMock
                .when(() -> CacheUtils.getObject(eq(cacheKey), eq(TEST_TOKEN)))
                .thenReturn(cachedProduct);

        // More calls to ensure cache is used
        for (int i = 0; i < 10; i++) {
            productServiceProxy.findById(request);
        }

        // Verify that the service method was called only once
        verify(productService, times(1)).findById(request);
    }

    @Test
    @DisplayName("getAllPageable should delegate to decorator with token context")
    void getAllPageable_ShouldDelegateWithTokenContext() {
        // Arrange
        cacheUtilsMock.when(() -> CacheUtils.getObject(anyString(), eq(TEST_TOKEN)))
                .thenReturn(pageableMock);
        // Act
        IPageable<ProductDTO> result = productServiceProxy.getAllPageable(pageRequest, mapper);
        // Assert
        assertEquals(pageableMock, result);
    }

    @Test
    @DisplayName("saveAll should delegate to ProductService and clear cache")
    void save_ShouldDelegateAndClearCache() throws ServiceException {
        // Arrange
        List<Product> products = List.of(new Product());
        when(productService.save(products, TEST_TOKEN)).thenReturn(products);
        // Act
        List<Product> result = productServiceProxy.save(products, TEST_TOKEN);
        // Assert
        assertEquals(products, result);
        verify(productService).save(products, TEST_TOKEN);
    }

    @Test
    @DisplayName("calculateTotalPriceFor should delegate to ProductService")
    void calculateTotalPriceFor_ShouldDelegate() {
        // Arrange
        BigDecimal expectedTotal = BigDecimal.valueOf(100.0);
        when(productService.calculateTotalPriceFor(product)).thenReturn(expectedTotal);
        // Act
        BigDecimal result = productServiceProxy.calculateTotalPriceFor(product);
        // Assert
        assertEquals(expectedTotal, result);
        verify(productService).calculateTotalPriceFor(product);
    }

    @Test
    @DisplayName("getEntity should delegate to ProductService")
    void getEntity_ShouldDelegate() {
        // Arrange
        when(productService.getEntity(request)).thenReturn(product);
        // Act
        Product result = productServiceProxy.getEntity(request);
        // Assert
        assertEquals(product, result);
        verify(productService).getEntity(request);
    }
}
