package com.dev.servlet.domain.service.cache;

import com.dev.servlet.application.dto.ProductDTO;
import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.core.decorator.CachedServiceDecorator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.domain.model.pojo.domain.Product;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.service.ProductService;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedProductServiceTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private CachedProductService cachedProductService;

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

    private static final String TEST_TOKEN = "test-token-with-sufficient-length-for-cache";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(request.token()).thenReturn(TEST_TOKEN);
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
        try (MockedStatic<CacheUtils> cacheUtilsMock = mockStatic(CacheUtils.class)) {
            // Mock CacheUtils.clearCacheKeyPrefix to avoid StringIndexOutOfBoundsException
            cacheUtilsMock.when(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN))).thenAnswer(invocation -> null);

            ProductDTO result = cachedProductService.create(request);

            // Assert
            assertEquals(productDTO, result);
            verify(productService).create(request);
            cacheUtilsMock.verify(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN)), times(1));
        }
    }

    @Test
    @DisplayName("update should delegate to ProductService and invalidate cache")
    void update_ShouldDelegateAndInvalidateCache() throws ServiceException {
        // Arrange
        when(productService.update(request)).thenReturn(productDTO);

        // Act
        try (MockedStatic<CacheUtils> cacheUtilsMock = mockStatic(CacheUtils.class)) {
            // Mock CacheUtils.clearCacheKeyPrefix to avoid StringIndexOutOfBoundsException
            cacheUtilsMock.when(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN))).thenAnswer(invocation -> null);

            ProductDTO result = cachedProductService.update(request);

            // Assert
            assertEquals(productDTO, result);
            verify(productService).update(request);
            cacheUtilsMock.verify(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN)), times(1));
        }
    }

    @Test
    @DisplayName("delete should delegate to ProductService and invalidate cache")
    void delete_ShouldDelegateAndInvalidateCache() throws ServiceException {
        // Act
        try (MockedStatic<CacheUtils> cacheUtilsMock = mockStatic(CacheUtils.class)) {
            // Mock CacheUtils.clearCacheKeyPrefix to avoid StringIndexOutOfBoundsException
            cacheUtilsMock.when(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN))).thenAnswer(invocation -> null);

            cachedProductService.delete(request);

            // Assert
            verify(productService).delete(request);
            cacheUtilsMock.verify(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN)), times(1));
        }
    }

    @Test
    @DisplayName("findAll should delegate to ProductService when filter is null")
    void findAll_ShouldDelegateToProductServiceWhenFilterIsNull() {
        // Arrange
        when(productService.getEntity(request)).thenReturn(null);
        when(productService.findAll(request)).thenReturn(List.of(1L, 2L));

        // Act
        Collection<Long> result = cachedProductService.findAll(request);

        // Assert
        verify(productService).getEntity(request);
        verify(productService).findAll(request);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }

    @Test
    @DisplayName("findAll should use cache when available")
    void findAll_ShouldUseCacheWhenAvailable() {
        // Arrange
        Product filter = new Product();
        filter.setId(1L);

        when(productService.getEntity(request)).thenReturn(filter);

        List<Product> products = List.of(
                new Product("Product 1", "Description 1", BigDecimal.valueOf(10.0)),
                new Product("Product 2", "Description 2", BigDecimal.valueOf(20.0))
        );
        products.get(0).setId(1L);
        products.get(1).setId(2L);

        // Create a real CachedServiceDecorator with our mock productService
        CachedServiceDecorator<Product, Long> decorator = new CachedServiceDecorator<>(
                productService, "product", TEST_TOKEN);

        // Create a spy of the cachedProductService to intercept getDecoratorForToken
        CachedProductService serviceSpy = spy(cachedProductService);
        doReturn(decorator).when(serviceSpy).getDecoratorForToken(TEST_TOKEN);

        // Mock the productService.findAll to return our products
        when(productService.findAll(any(Product.class))).thenReturn(products);

        // Act
        Collection<Long> result = serviceSpy.findAll(request);

        // Assert
        verify(productService).getEntity(request);
        verify(productService).findAll(any(Product.class));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getById should use cache when available")
    void getById_ShouldUseCacheWhenAvailable() throws ServiceException {
        // Arrange
        Product filter = new Product();
        filter.setId(1L);

        when(productService.getEntity(request)).thenReturn(filter);
        when(productService.getById(request)).thenReturn(productDTO);

        Product cachedProduct = new Product("Cached Product", "Cached Description", BigDecimal.valueOf(15.0));
        cachedProduct.setId(1L);

        // Act & Assert
        try (MockedStatic<CacheUtils> cacheUtilsMock = mockStatic(CacheUtils.class)) {
            // First call - cache miss
            cacheUtilsMock.when(() -> CacheUtils.getObject(anyString(), eq(TEST_TOKEN)))
                    .thenReturn(null);

            ProductDTO result1 = cachedProductService.getById(request);

            assertEquals(productDTO, result1);
            verify(productService).getById(request);

            // Second call - cache hit
            cacheUtilsMock.when(() -> CacheUtils.getObject(anyString(), eq(TEST_TOKEN)))
                    .thenReturn(cachedProduct);

            // Mock the static ProductMapper.full method
            try (MockedStatic<ProductMapper> productMapperMock = mockStatic(ProductMapper.class)) {
                productMapperMock.when(() -> ProductMapper.full(cachedProduct)).thenReturn(productDTO);

                ProductDTO result2 = cachedProductService.getById(request);

                assertEquals(productDTO, result2);
                // ProductService.getById should not be called again
                verify(productService, times(1)).getById(request);
            }
        }
    }

    @Test
    @DisplayName("getAllPageable should delegate to decorator with token context")
    void getAllPageable_ShouldDelegateWithTokenContext() {
        // Arrange

        // Act
        try (MockedStatic<CacheUtils> cacheUtilsMock = mockStatic(CacheUtils.class)) {
            cacheUtilsMock.when(() -> CacheUtils.getObject(anyString(), eq(TEST_TOKEN)))
                    .thenReturn(pageableMock);

            IPageable<ProductDTO> result = cachedProductService.getAllPageable(pageRequest, mapper);

            // Assert
            assertEquals(pageableMock, result);
        }
    }

    @Test
    @DisplayName("saveAll should delegate to ProductService and clear cache")
    void saveAll_ShouldDelegateAndClearCache() throws ServiceException {
        // Arrange
        List<Product> products = List.of(new Product());
        when(productService.saveAll(products)).thenReturn(products);

        // Act
        List<Product> result = cachedProductService.saveAll(products, TEST_TOKEN);

        // Assert
        assertEquals(products, result);
        verify(productService).saveAll(products);
        verify(productService).clearCache(TEST_TOKEN);
    }

    @Test
    @DisplayName("calculateTotalPriceFor should delegate to ProductService")
    void calculateTotalPriceFor_ShouldDelegate() {
        // Arrange
        BigDecimal expectedTotal = BigDecimal.valueOf(100.0);
        when(productService.calculateTotalPriceFor(product)).thenReturn(expectedTotal);

        // Act
        BigDecimal result = cachedProductService.calculateTotalPriceFor(product);

        // Assert
        assertEquals(expectedTotal, result);
        verify(productService).calculateTotalPriceFor(product);
    }

    @Test
    @DisplayName("clearCache should remove from map and delegate to ProductService")
    void clearCache_ShouldRemoveFromMapAndDelegate() {
        // Act
        cachedProductService.clearCache(TEST_TOKEN);

        // Assert
        verify(productService).clearCache(TEST_TOKEN);
    }

    @Test
    @DisplayName("getEntity should delegate to ProductService")
    void getEntity_ShouldDelegate() {
        // Arrange
        when(productService.getEntity(request)).thenReturn(product);

        // Act
        Product result = cachedProductService.getEntity(request);

        // Assert
        assertEquals(product, result);
        verify(productService).getEntity(request);
    }
}
