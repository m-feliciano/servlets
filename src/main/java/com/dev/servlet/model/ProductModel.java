package com.dev.servlet.model;

import com.dev.servlet.dao.ProductDAO;
import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.model.shared.BusinessShared;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.Inventory;
import com.dev.servlet.pojo.Product;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.Query;
import com.dev.servlet.pojo.records.Request;
import lombok.NoArgsConstructor;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Product Business
 * <p>
 * This class is responsible for handling the product business logic.
 *
 * @see BaseModel
 */
@NoArgsConstructor
@Model
public class ProductModel extends BaseModel<Product, Long> {

    public static final String NOT_FOUND = "Product #%s not found.";

    @Inject
    private BusinessShared businessShared;

    @Inject
    protected ProductModel(ProductDAO dao) {
        super(dao);
    }

    protected ProductDAO getDAO() {
        return (ProductDAO) super.getBaseDAO();
    }

    @Override
    protected Class<? extends Identifier<Long>> getTransferClass() {
        return ProductDTO.class;
    }

    @Override
    protected Product toEntity(Object object) {
        return ProductMapper.full((ProductDTO) object);
    }

    @Override
    protected Product getEntity(Request request) {
        Product product = super.getEntity(request);
        product = Optional.ofNullable(product).orElse(new Product());

        String categoryId = request.getParameter("category");
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
     * Create one
     *
     * @param request {@link Request}
     * @return the next path
     */
    // @ResourceMapping(CREATE)
    public ProductDTO create(Request request) {
        LOGGER.trace("");

        Product product = this.getEntity(request);
        product.setUser(getUser(request.getToken()));
        product.setRegisterDate(new Date());
        product.setStatus(StatusEnum.ACTIVE.getValue());

        super.save(product);

        // super.redirectTo(product.getId())
        return ProductMapper.full(product);
    }

    /**
     * List all products
     *
     * @param request {@link Request}
     * @return {@link Collection} of {@link ProductDTO}
     */
    public Collection<Long> findAll(Request request) {
        LOGGER.trace("");

        Product filter = this.getEntity(request);
        filter.setUser(getUser(request.getToken()));

        return super.findAllOnlyIds(filter);
    }

    /**
     * Get by id
     *
     * @param request
     * @return
     * @throws ServiceException
     */
    public ProductDTO getById(Request request) throws ServiceException {
        LOGGER.trace("");

        long entityId = Long.parseLong(request.getEntityId());

        Product product = this.findById(request.getToken(), entityId)
                .orElseThrow(() -> new ServiceException(404, NOT_FOUND.formatted(entityId)));

        return ProductMapper.full(product);
    }

    /**
     * Update one
     *
     * @param request {@link Request}
     * @return the next path
     */
    public ProductDTO update(Request request) throws ServiceException {
        LOGGER.trace("");

        Long id = Long.parseLong(request.getEntityId());

        Product product = this.findById(request.getToken(), id).orElseThrow(
                () -> new ServiceException(404, "Product #" + id + " not found."));

        Product productRequest = this.getEntity(request);

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setUrl(productRequest.getUrl());
        product.setCategory(productRequest.getCategory());

        super.update(product);
        return ProductMapper.full(product);
    }

    /**
     * Delete one
     *
     * @param request {@link Request}
     * @return the next path
     */
    public void delete(Request request) throws ServiceException {
        LOGGER.trace("");

        Long entityId = Long.parseLong(request.getEntityId());

        Product product = this.findById(request.getToken(), entityId).orElseThrow(
                () -> new ServiceException(404, "Product #" + entityId + " not found."));

        Inventory inventory = new Inventory();
        inventory.setUser(getUser(request.getToken()));
        inventory.setProduct(product);

        if (businessShared.hasInventory(inventory)) {
            throw new ServiceException(HttpServletResponse.SC_CONFLICT, "Product has inventory.");
        }

        super.delete(product);
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

    /**
     * Calculate total price
     *
     * @param productIds the product ids
     * @return {@link BigDecimal} total price
     */
    public BigDecimal calculateTotalPrice(Collection<Long> productIds) {
        return this.getDAO().calculateTotalPrice(productIds);
    }
}
