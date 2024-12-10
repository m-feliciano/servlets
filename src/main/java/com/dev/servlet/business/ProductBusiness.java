package com.dev.servlet.business;

import com.dev.servlet.business.shared.BusinessShared;
import com.dev.servlet.controllers.ProductController;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.interfaces.ResourceMapping;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.Inventory;
import com.dev.servlet.pojo.Product;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.Query;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Product Business
 * <p>
 * This class is responsible for handling the product business logic.
 *
 * @see BaseBusiness
 */
@Setter
@NoArgsConstructor
@Singleton
@ResourcePath("product")
public class ProductBusiness extends BaseBusiness<Product, Long, ProductDTO> {

    private static final String PRODUCT = "product";
    private static final String CATEGORIES = "categories";

    @Inject
    private CategoryBusiness categoryBusiness;
    @Inject
    private BusinessShared businessShared;

    @Inject
    protected ProductBusiness(ProductController controller) {
        super(controller);
        this.mapper = new ProductMapper();
    }

    @Override
    protected ProductController getController() {
        return (ProductController) super.getController();
    }

    @Override
    protected Product getEntity(Request request) {
        Product product = super.getEntity(request);
        product = Optional.ofNullable(product).orElse(new Product());

        String categoryId = request.getParameter(CATEGORY);
        if (categoryId != null && !categoryId.isBlank()) {
            product.setCategory(new Category(Long.valueOf(categoryId)));
        }

        Query query = request.getQuery();
        if (query.getSearch() != null && query.getType() != null) {
            if (query.getType().equals("name")) {
                product.setName(query.getSearch());
            } else if (query.getType().equals("description")) {
                product.setDescription(query.getSearch());
            }
        }

        return product;
    }


    /**
     * Forward
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping(NEW)
    public Response forwardRegister(Request request, String token) {
        LOGGER.trace("");

        Response response = new Response(HttpServletResponse.SC_FOUND);
        response.data(CATEGORIES, categoryBusiness.getAllFromCache(token));
        return response.next(super.forwardTo("formCreateProduct"));
    }

    /**
     * Create one
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping(CREATE)
    public Response register(Request request, String token) {
        LOGGER.trace("");

        Product product = this.getEntity(request);
        product.setUser(getUser(token));
        product.setRegisterDate(new Date());
        product.setStatus(StatusEnum.ACTIVE.getValue());

        super.save(product);

        return super.createResponse(HttpServletResponse.SC_CREATED, product, super.redirectTo(product.getId()));
    }

    /**
     * Forward edit
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping(EDIT)
    public Response edit(Request request, String token) {
        LOGGER.trace("");

        long entityId = Long.parseLong(request.getEntityId());
        Optional<Product> optional = this.findById(token, entityId);
        if (optional.isEmpty()) {
            return super.responseEntityNotFound(entityId);
        }

        List<CategoryDTO> categories = categoryBusiness.getAllFromCache(token);

        var responseData = new Response.Data()
                .add(PRODUCT, optional.get())
                .add(CATEGORIES, categories);

        return Response.of(responseData).next(super.forwardTo("formUpdateProduct"));
    }

    /**
     * List one or many (with query)
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping(LIST)
    public Response getAll(Request request, String token) {
        LOGGER.trace("");

        if (request.getEntityId() != null) {
            long entityId = Long.parseLong(request.getEntityId());

            Optional<Product> optional = this.findById(token, entityId);
            if (optional.isEmpty()) {
                return super.responseEntityNotFound(entityId);
            }

            return super.createResponse(HttpServletResponse.SC_OK, optional.get(), super.forwardTo("formListProduct"));
        }

        Product filter = this.getEntity(request);
        filter.setUser(getUser(token));

        Query query = request.getQuery();
        Collection<Long> productIds = super.findAllOnlyIds(filter);
        query.getPagination().setTotalRecords(productIds.size());

        var categoryList = categoryBusiness.getAllFromCache(token);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ProductDTO> products = List.of();
        if (!productIds.isEmpty()) {
            totalPrice = this.getController().calculateTotalPrice(productIds);

            products = super.getAllPageable(productIds, query.getPagination()).stream()
                    .map(super::fromEntity)
                    .toList();
        }

        var responseData = new Response.Data()
                .add("products", products)
                .add(CATEGORIES, categoryList)
                .add("totalPrice", totalPrice);

        return Response.of(responseData).next(super.forwardTo("listProducts"));
    }

    /**
     * Update one
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping(UPDATE)
    public Response update(Request request, String token) {
        LOGGER.trace("");

        Long entityId = Long.parseLong(request.getEntityId());
        Optional<Product> optional = this.findById(token, entityId);
        if (optional.isEmpty()) {
            return super.responseEntityNotFound(entityId);
        }

        Product productRequest = this.getEntity(request);

        Product product = optional.get();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setUrl(productRequest.getUrl());
        product.setCategory(productRequest.getCategory());

        super.update(product);

        return super.createResponse(HttpServletResponse.SC_OK, product, super.redirectTo(product.getId()));
    }

    /**
     * Delete one
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping(DELETE)
    public Response delete(Request request, String token) {
        LOGGER.trace("");

        Long entityId = Long.parseLong(request.getEntityId());

        Optional<Product> optional = this.findById(token, entityId);
        if (optional.isEmpty()) {
            return super.responseEntityNotFound(entityId);
        }

        Product product = optional.get();

        Inventory inventory = new Inventory();
        inventory.setUser(getUser(token));
        inventory.setProduct(product);

        if (businessShared.hasInventory(inventory)) {
            return Response.ofError(HttpServletResponse.SC_CONFLICT, "Product has inventory.");
        }

        super.delete(product);

        return super.createResponse(HttpServletResponse.SC_NO_CONTENT, null, super.redirectTo(LIST));
    }

    /**
     * Get by id
     *
     * @param token the user token
     * @param id    the product id
     * @return {@link Optional} of {@link Product}
     */
    private Optional<Product> findById(String token, Long id) {
        if (id == null) return Optional.empty();

        Product product = new Product(id);
        product.setUser(getUser(token));
        product = super.find(product);

        return Optional.ofNullable(product);
    }
}
