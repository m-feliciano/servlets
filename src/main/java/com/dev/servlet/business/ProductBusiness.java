package com.dev.servlet.business;

import com.dev.servlet.controllers.ProductController;
import com.dev.servlet.domain.Category;
import com.dev.servlet.domain.Inventory;
import com.dev.servlet.domain.Product;
import com.dev.servlet.domain.enums.StatusEnum;
import com.dev.servlet.dto.CategoryDto;
import com.dev.servlet.dto.ProductDto;
import com.dev.servlet.filter.StandardRequest;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.mapper.CategoryMapper;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.utils.CurrencyFormatter;
import com.dev.servlet.business.base.BaseRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Product Business
 * <p>
 * This class is responsible for handling the product business logic.
 *
 * @see BaseRequest
 */
@Singleton
public class ProductBusiness extends BaseRequest {

    private static final String FORWARD_PAGE_LIST = "forward:pages/product/formListProduct.jsp";
    private static final String FORWARD_PAGE_LIST_PRODUCTS = "forward:pages/product/listProducts.jsp";
    private static final String FORWARD_PAGE_UPDATE = "forward:pages/product/formUpdateProduct.jsp";
    private static final String FORWARD_PAGE_CREATE = "forward:pages/product/formCreateProduct.jsp";
    private static final String REDIRECT_ACTION_LIST_ALL = "redirect:product?action=list";
    private static final String REDIRECT_ACTION_LIST_BY_ID = "redirect:product?action=list&id=";

    private static final String CACHE_KEY = "categories";

    @Inject
    private ProductController controller;
    @Inject
    private CategoryBusiness categoryBusiness;
    @Inject
    private ProductShared productShared;

    public ProductBusiness() {
    }

    public ProductBusiness(ProductController controller,
                           CategoryBusiness categoryBusiness,
                           ProductShared productShared) {
        this.controller = controller;
        this.categoryBusiness = categoryBusiness;
        this.productShared = productShared;
    }

    /**
     * Forward
     *
     * @return the next path
     */
    @ResourcePath(value = NEW)
    public String forwardRegister(StandardRequest request) {
        List<CategoryDto> categories = categoryBusiness.findAll(request);
        request.servletRequest().setAttribute("categories", categories);
        return FORWARD_PAGE_CREATE;
    }

    /**
     * Create one
     *
     * @param request
     * @return the next path
     */
    @ResourcePath(value = CREATE)
    public String register(StandardRequest request) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(LocalDate.now().format(formatter), formatter);

        Product product = new Product(
                getParameter(request, "name"),
                getParameter(request, "description"),
                getParameter(request, "url"),
                localDate,
                CurrencyFormatter.stringToBigDecimal(getParameter(request, "price")));

        product.setUser(getUser(request));
        product.setCategory(new Category(Long.valueOf(getParameter(request, "category"))));
        product.setStatus(StatusEnum.ACTIVE.getName());
        controller.save(product);
        request.servletRequest().setAttribute("product", product);

        return REDIRECT_ACTION_LIST_BY_ID + product.getId();
    }

    /**
     * Forward edit
     *
     * @param request
     * @return the next path
     */
    @ResourcePath(value = EDIT)
    public String edit(StandardRequest request) {
        String id = getParameter(request, "id");
        if (id == null) {
            request.servletRequest().setAttribute("error", "id can't be null");
            return FORWARD_PAGES_NOT_FOUND;
        }

        Product product = new Product();
        product = controller.findById(Long.valueOf(id));

        request.servletRequest().setAttribute("product", ProductMapper.from(product));
        request.servletRequest().setAttribute("categories", categoryBusiness.findAll(request));

        return FORWARD_PAGE_UPDATE;
    }

    /**
     * List one or many
     *
     * @param request
     * @return the next path
     */
    @ResourcePath(value = LIST)
    public String list(StandardRequest request) {
        String id = getParameter(request, "id");
        if (id != null) {
            Product product = controller.findById(Long.valueOf(id));
            if (product == null) {
                return FORWARD_PAGES_NOT_FOUND;
            }
            request.servletRequest().setAttribute("product", ProductMapper.from(product));
            return FORWARD_PAGE_LIST;
        }

        Product product = new Product();
        product.setUser(getUser(request));

        String param = getParameter(request, PARAM);
        String value = getParameter(request, VALUE);
        if (param != null && value != null) {
            if (param.equals("name")) {
                product.setName(value);
            } else {
                product.setDescription(value);
            }
        }

        List<ProductDto> products = findAll(product);
        request.servletRequest().setAttribute("products", products);

        String categoryId = getParameter(request, "categoryId");
        List<Category> categories = categoryBusiness.findAll(request).stream().map(CategoryMapper::from).toList();
        request.servletRequest().setAttribute("categories", categories);

        return FORWARD_PAGE_LIST_PRODUCTS;
    }

    /**
     * Update one
     *
     * @param request
     * @return the next path
     */
    @ResourcePath(value = UPDATE)
    public String update(StandardRequest request) {

        Product product = controller.findById(Long.parseLong(getParameter(request, "id")));
        product.setName(getParameter(request, "name"));
        product.setDescription(getParameter(request, "description"));
        product.setPrice(CurrencyFormatter.stringToBigDecimal(getParameter(request, "price")));
        product.setUrl(getParameter(request, "url"));
        product.setCategory(new Category(Long.parseLong(getParameter(request, "category"))));

        product = controller.update(product);
        request.servletRequest().setAttribute("product", ProductMapper.from(product));

        return REDIRECT_ACTION_LIST_BY_ID + product.getId();
    }

    /**
     * Delete one
     *
     * @param request
     * @return the next path
     */
    @ResourcePath(value = DELETE)
    public String delete(StandardRequest request) {
        Long id = Long.parseLong(getParameter(request, "id"));
        Product product = new Product(id);
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        if (productShared.hasInventory(inventory)) {
            request.servletRequest().setAttribute("error", "Product has inventory");
            return FORWARD_PAGES_NOT_FOUND;
        }

        controller.delete(product);
        return REDIRECT_ACTION_LIST_ALL;
    }

    /**
     * Find All
     *
     * @param product
     * @return the next path
     */
    public List<ProductDto> findAll(Product product) {
        List<Product> products = controller.findAll(product);
        return products.stream().map(ProductMapper::from).toList();
    }

    /**
     * Find by id
     *
     * @param product
     * @return the next path
     */
    public ProductDto find(Product product) {
        Product p = controller.find(product);
        return ProductMapper.from(p);
    }
}