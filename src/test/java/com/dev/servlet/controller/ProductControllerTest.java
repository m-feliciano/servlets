package com.dev.servlet.controller;

import com.dev.servlet.domain.transfer.dto.CategoryDTO;
import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.transfer.records.Query;
import com.dev.servlet.domain.transfer.records.Sort;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.domain.transfer.response.IServletResponse;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.service.ICategoryService;
import com.dev.servlet.domain.service.IProductService;
import com.dev.servlet.infrastructure.persistence.IPageable;
import com.dev.servlet.infrastructure.persistence.internal.PageRequest;
import com.dev.servlet.infrastructure.persistence.internal.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Mock
    private IProductService productService;

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private ProductController productController;

    @Mock
    private Request request;

    @Mock
    private PageRequest<Object> pageRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(request.getToken()).thenReturn("fakeToken");
        when(request.getQuery()).thenReturn(mock(Query.class));
        when(request.getQuery().getPageRequest()).thenReturn(pageRequest);
//        when(request.query().getPageRequestImpl().getRecords()).thenReturn(List.of(1L, 2L));
    }

    @Test
    @DisplayName(
            "Test create method to add a new product. " +
            "It should return a 201 status code and the expected response.")
    void testCreateProduct() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);

        when(productService.create(request)).thenReturn(productDTO);

        IHttpResponse<Void> response = productController.create(request);

        assertNotNull(response);
        assertEquals(201, response.statusCode());
        verify(productService, times(1)).create(request);
    }

    @Test
    @DisplayName(
            "Test forward method to create form. " +
            "It should return a 302 status code and the expected response.")
    void testForwardToCreateForm() {
        Collection<CategoryDTO> categories = List.of(new CategoryDTO());
        when(categoryService.list(any())).thenReturn(categories);

        IHttpResponse<Collection<CategoryDTO>> response = productController.forward(request);

        assertNotNull(response);
        assertEquals(302, response.statusCode());
        assertEquals(categories, response.body());
        verify(categoryService, times(1)).list(any());
    }

    @Test
    @DisplayName(
            "Test edit method to update a product. " +
            "It should return a 200 status code and the expected response.")
    void testEditProduct() throws ServiceException {
        ProductDTO productDTO = new ProductDTO();
        Collection<CategoryDTO> categories = List.of(new CategoryDTO());

        when(categoryService.list(any())).thenReturn(categories);
        when(productService.findById(request)).thenReturn(productDTO);

        IServletResponse response = productController.edit(request);

        assertNotNull(response);
        verify(productService, times(1)).findById(request);
        verify(categoryService, times(1)).list(any());
    }

    @Test
    @DisplayName(
            "Test listProducts method to retrieve a list of products. " +
            "It should return a 200 status code and the expected response.")
    @SuppressWarnings("unchecked")
    void testListProducts() {
        // Setup
        Product filterMock = new Product("prod", "desc", null);
        when(productService.getEntity(any())).thenReturn(filterMock);

        var categories = List.of(new CategoryDTO());
        when(categoryService.list(any())).thenReturn(categories);

        var products = List.of(
                ProductMapper.base(new Product("prod1", "desc1", BigDecimal.valueOf(50))),
                ProductMapper.base(new Product("prod2", "desc2", BigDecimal.valueOf(50)))
        );

        var pageableMock = PageResponse.<ProductDTO>builder()
                .content(products)
                .currentPage(1)
                .pageSize(2)
                .sort(Sort.by("id").ascending())
                .build();

        when(productService.getAllPageable(any(), any(Mapper.class))).thenReturn(pageableMock);
        when(productService.calculateTotalPriceFor(any())).thenReturn(BigDecimal.valueOf(100));

        // Execution
        IServletResponse response = productController.list(request);

        // Verification
        assertNotNull(response);
        assertEquals(200, response.statusCode());

        // Verify pageable content
        var pageable = response.body().stream()
                .filter(pair -> "pageable".equals(pair.key()))
                .findFirst()
                .map(e -> (IPageable<ProductDTO>) e.value())
                .orElseThrow(() -> new AssertionError("Pageable not found"));

        long counter = pageable.getContent().size();
        assertEquals(2, counter);

        // Verify total price
        BigDecimal totalPrice = response.body().stream()
                .filter(pair -> "totalPrice".equals(pair.key()))
                .findFirst()
                .map(e -> (BigDecimal) e.value())
                .orElse(null);

        assertEquals(BigDecimal.valueOf(100), totalPrice);

        // Verify categories
        var responseCategories = response.body().stream()
                .filter(pair -> "categories".equals(pair.key()))
                .findFirst()
                .map(e -> (Collection<CategoryDTO>) e.value())
                .orElse(null);

        assertEquals(categories, responseCategories);

        // Verify interactions
        verify(productService, times(1)).getEntity(request);
        verify(productService, times(1)).getAllPageable(any(), any(Mapper.class));
        verify(productService, times(1)).calculateTotalPriceFor(filterMock);
        verify(categoryService, times(1)).list(any());
    }

    @Test
    @DisplayName(
            "Test delete method to remove a product. " +
            "It should return a 200 status code and the expected response.")
    void testDeleteProduct() throws ServiceException {
        doReturn(true).when(productService).delete(request);

        IHttpResponse<Void> response = productController.delete(request);

        assertNotNull(response);
        assertEquals(200, response.statusCode());
        verify(productService, times(1)).delete(request);
    }

    @Test
    @DisplayName(
            "Test listById method to retrieve a product by ID. " +
            "It should return a 200 status code and the expected response.")
    void testGetById() throws ServiceException {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);

        when(productService.findById(request)).thenReturn(productDTO);

        IHttpResponse<ProductDTO> response = productController.getById(request);
        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertEquals(productDTO, response.body());
        verify(productService, times(1)).findById(request);
    }
}
