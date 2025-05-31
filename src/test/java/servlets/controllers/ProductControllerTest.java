package servlets.controllers;

import com.dev.servlet.controller.CategoryController;
import com.dev.servlet.controller.ProductController;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.IServletResponse;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.model.Mapper;
import com.dev.servlet.model.impl.ProductModel;
import com.dev.servlet.model.pojo.domain.Product;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Query;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.model.pojo.records.Sort;
import com.dev.servlet.persistence.IPageable;
import com.dev.servlet.persistence.impl.PageRequestImpl;
import com.dev.servlet.persistence.impl.PageableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Mock
    private ProductModel productModel;

    @Mock
    private CategoryController categoryController;

    @InjectMocks
    private ProductController productController;

    @Mock
    private Request request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productController.setCategoryController(categoryController);

        when(request.token()).thenReturn("fakeToken");
        when(request.query()).thenReturn(mock(Query.class));
        when(request.query().getPageRequest()).thenReturn(mock(PageRequestImpl.class));
//        when(request.query().getPageRequestImpl().getRecords()).thenReturn(List.of(1L, 2L));
    }

    @Test
    @DisplayName(
            "Test create method to add a new product. " +
            "It should return a 201 status code and the expected response.")
    void testCreateProduct() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);

        when(productModel.create(request)).thenReturn(productDTO);

        IHttpResponse<Void> response = productController.create(request);

        assertNotNull(response);
        assertEquals(201, response.statusCode());
        verify(productModel, times(1)).create(request);
    }

    @Test
    @DisplayName(
            "Test forward method to create form. " +
            "It should return a 302 status code and the expected response.")
    void testForwardToCreateForm() {
        Collection<CategoryDTO> categories = List.of(new CategoryDTO());
        var categoryResponse = HttpResponseImpl.<Collection<CategoryDTO>>newBuilder().body(categories).build();
        when(categoryController.list(any())).thenReturn(categoryResponse);

        IHttpResponse<Collection<CategoryDTO>> response = productController.forward(request);

        assertNotNull(response);
        assertEquals(302, response.statusCode());
        assertEquals(categories, response.body());
        verify(categoryController, times(1)).list(any());
    }

    @Test
    @DisplayName(
            "Test edit method to update a product. " +
            "It should return a 200 status code and the expected response.")
    void testEditProduct() throws ServiceException {
        ProductDTO productDTO = new ProductDTO();
        Collection<CategoryDTO> categories = List.of(new CategoryDTO());

        var categoryResponse = HttpResponseImpl.<Collection<CategoryDTO>>newBuilder().body(categories).build();
        when(categoryController.list(any())).thenReturn(categoryResponse);
        when(productModel.getById(request)).thenReturn(productDTO);

        IServletResponse response = productController.edit(request);

        assertNotNull(response);
        verify(productModel, times(1)).getById(request);
        verify(categoryController, times(1)).list(any());
    }

    @Test
    @DisplayName(
            "Test listProducts method to retrieve a list of products. " +
            "It should return a 200 status code and the expected response.")
    @SuppressWarnings("unchecked")
    void testListProducts() {
        // Setup
        Product filterMock = new Product("prod", "desc", null);
        when(productModel.getEntity(any())).thenReturn(filterMock);

        var categories = List.of(new CategoryDTO());
        var categoryResponse = HttpResponseImpl.<Collection<CategoryDTO>>newBuilder().body(categories).build();
        when(categoryController.list(any())).thenReturn(categoryResponse);

        var products = List.of(
                ProductMapper.base(new Product("prod1", "desc1", BigDecimal.valueOf(50))),
                ProductMapper.base(new Product("prod2", "desc2", BigDecimal.valueOf(50)))
        );

        var pageableMock = PageableImpl.<ProductDTO>builder()
                .content(products)
                .currentPage(1)
                .pageSize(2)
                .sort(Sort.by("id").ascending())
                .build();

        when(productModel.getAllPageable(any(), any(Mapper.class))).thenReturn(pageableMock);
        when(productModel.calculateTotalPriceFor(any())).thenReturn(BigDecimal.valueOf(100));

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

        long counter = StreamSupport.stream(pageable.getContent().spliterator(), false).count();
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
        verify(productModel, times(1)).getEntity(request);
        verify(productModel, times(1)).getAllPageable(any(), any(Mapper.class));
        verify(productModel, times(1)).calculateTotalPriceFor(filterMock);
        verify(categoryController, times(1)).list(any());
    }

    @Test
    @DisplayName(
            "Test delete method to remove a product. " +
            "It should return a 200 status code and the expected response.")
    void testDeleteProduct() throws ServiceException {
        doNothing().when(productModel).delete(request);

        IHttpResponse<Void> response = productController.delete(request);

        assertNotNull(response);
        assertEquals(200, response.statusCode());
        verify(productModel, times(1)).delete(request);
    }

    @Test
    @DisplayName(
            "Test listById method to retrieve a product by ID. " +
            "It should return a 200 status code and the expected response.")
    void testListById() throws ServiceException {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);

        when(productModel.getById(request)).thenReturn(productDTO);

        IHttpResponse<ProductDTO> response = productController.listById(request);
        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertEquals(productDTO, response.body());
        verify(productModel, times(1)).getById(request);
    }
}