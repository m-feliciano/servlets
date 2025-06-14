package com.dev.servlet.model.impl;

import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.ProductMapper;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.domain.Category;
import com.dev.servlet.model.pojo.domain.Inventory;
import com.dev.servlet.model.pojo.domain.Product;
import com.dev.servlet.model.pojo.enums.Status;
import com.dev.servlet.model.pojo.records.Query;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.model.shared.BusinessShared;
import com.dev.servlet.persistence.dao.ProductDAO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.dev.servlet.util.ThrowableUtils.throwIfTrue;

/**
 * Product Business
 * <p>
 * This class is responsible for handling the product business logic.
 *
 * @see BaseModel
 */
@Slf4j
@NoArgsConstructor
@Model
public class ProductModel extends BaseModel<Product, Long> {

    public static final String DESCRIPTION = "description";
    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final int ERROR_CODE_404 = 404;

    private BusinessShared businessShared;

    @Inject
    public void setBusinessShared(BusinessShared businessShared) {
        this.businessShared = businessShared;
    }

    @Inject
    protected ProductModel(ProductDAO dao) {
        super(dao);
    }

    protected ProductDAO getDAO() {
        return (ProductDAO) super.getBaseDAO();
    }

    @Override
    protected Class<ProductDTO> getTransferClass() {
        return ProductDTO.class;
    }

    @Override
    protected Product toEntity(Object object) {
        return ProductMapper.full((ProductDTO) object);
    }

    @Override
    public Product getEntity(Request request) {
        Product product = super.getEntity(request);
        product = Optional.ofNullable(product).orElse(new Product());

        String categoryId = request.getParameter(CATEGORY);
        if (categoryId != null && !categoryId.isBlank()) {
            product.setCategory(new Category(Long.valueOf(categoryId)));
        }

        Query query = request.query();
        if (query.getSearch() != null && query.getType() != null) {
            if (query.getType().equals(NAME)) {
                product.setName(query.getSearch());
            } else if (query.getType().equals(DESCRIPTION)) {
                product.setDescription(query.getSearch());
            }
        }

        product.setUser(getUser(request.token()));
        return product;
    }

    /**
     * Create one
     *
     * @param request {@linkplain Request}
     * @return the next path
     */
    public ProductDTO create(Request request) {
        log.trace("");

        Product product = this.getEntity(request);
        product.setRegisterDate(new Date());
        product.setStatus(Status.ACTIVE.getValue());

        product = super.save(product);
        return ProductMapper.full(product);
    }

    public List<Product> saveAll(List<Product> products) throws ServiceException {
        return baseDAO.saveAll(products);
    }

    /**
     * List all products
     *
     * @param request {@linkplain Request}
     * @return {@linkplain Collection} of {@linkplain ProductDTO}
     */
    public Collection<Long> findAll(Request request) {
        log.trace("");

        Product filter = this.getEntity(request);
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
        log.trace("");

        Product filter = getEntity(request);
        Optional<Product> optional = this.findById(filter);

        throwIfTrue(optional.isEmpty(), ERROR_CODE_404, "Product not found.");

        return ProductMapper.full(optional.get());
    }

    /**
     * Update one
     *
     * @param request {@linkplain Request}
     * @return the next path
     */
    public ProductDTO update(Request request) throws ServiceException {
        log.trace("");

        Product productRequest = getEntity(request);
        Optional<Product> optional = this.findById(productRequest);

        throwIfTrue(optional.isEmpty(), ERROR_CODE_404, "Product not found.");

        Product product = optional.get();
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
     * @param request {@linkplain Request}
     * @return the next path
     */
    public void delete(Request request) throws ServiceException {
        log.trace("");

        Product productRequest = getEntity(request);

        Optional<Product> optional = this.findById(productRequest);

        throwIfTrue(optional.isEmpty(), ERROR_CODE_404, "Product not found.");

        Product product = optional.get();
        Inventory inventory = new Inventory();
        inventory.setUser(productRequest.getUser());
        inventory.setProduct(product);

        if (businessShared.hasInventory(inventory)) {
            throw new ServiceException(HttpServletResponse.SC_CONFLICT, "Product has inventory.");
        }

        super.delete(product);
    }

    /**
     * Get by id
     *
     * @param filter - product filter
     * @return {@linkplain Optional} of {@linkplain Product}
     */
    private Optional<Product> findById(Product filter) {

        Product product = super.find(filter);
        return Optional.ofNullable(product);
    }

    /**
     * Calculate total price
     *
     * @param filter - product filter
     * @return {@linkplain BigDecimal} total price
     */
    public BigDecimal calculateTotalPriceFor(Product filter) {
        return this.getDAO().calculateTotalPriceFor(filter);
    }
}
