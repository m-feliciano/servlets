package com.dev.servlet.model.impl;

import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.InventoryMapper;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.domain.Category;
import com.dev.servlet.model.pojo.domain.Inventory;
import com.dev.servlet.model.pojo.domain.Product;
import com.dev.servlet.model.pojo.enums.Status;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.model.shared.BusinessShared;
import com.dev.servlet.persistence.dao.InventoryDAO;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.dev.servlet.util.ThrowableUtils.throwIfTrue;

/**
 * Inventory business.
 * <p>
 * This class is responsible for handling the inventory business logic.
 *
 * @see BaseModel
 * @since 1.0
 */
@Slf4j
@Setter
@NoArgsConstructor
@Model
public class InventoryModel extends BaseModel<Inventory, Long> {

    public static final String PRODUCT_ID = "productId";
    public static final String PRODUCT = "product";
    public static final String CATEGORY = "category";

    private BusinessShared businessShared;

    @Inject
    public void setBusinessShared(BusinessShared businessShared) {
        this.businessShared = businessShared;
    }

    @Inject
    public InventoryModel(InventoryDAO dao) {
        super(dao);
    }

    private InventoryDAO getDAO() {
        return (InventoryDAO) super.getBaseDAO();
    }

    @Override
    protected Class<InventoryDTO> getTransferClass() {
        return InventoryDTO.class;
    }

    @Override
    protected Inventory toEntity(Object object) {
        return InventoryMapper.full((InventoryDTO) object);
    }

    @Override
    protected Inventory getEntity(Request request) {
        log.trace("");

        Inventory inventory = super.getEntity(request);
        inventory = Optional.ofNullable(inventory).orElse(new Inventory());

        if (request.getParameter(PRODUCT_ID) != null) {
            Long productId = Long.parseLong(request.getParameter(PRODUCT_ID));
            inventory.setProduct(new Product(productId));
        }

        if (request.query().getType() != null && request.query().getSearch() != null) {
            Product product = new Product();

            if (PRODUCT.equals(request.query().getType())) {
                product.setId(Long.valueOf(request.query().getSearch().trim()));
                inventory.setProduct(product);
            } else if ("name".equals(request.query().getType())) {
                product.setName(request.query().getSearch().trim());

                inventory.setProduct(product);
            } else {
                inventory.setDescription(request.query().getSearch().trim());
            }

            String categoryId = request.getParameter(CATEGORY);
            if (categoryId != null && !categoryId.isEmpty()) {
                product.setCategory(new Category(Long.valueOf(categoryId)));
            }
        }

        inventory.setUser(getUser(request.token()));
        return inventory;
    }

    /**
     * Create inventory.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain InventoryDTO} the inventory
     * @throws ServiceException if any error occurs
     */
    public InventoryDTO create(Request request) throws ServiceException {
        log.trace("");

        Inventory inventory = this.getEntity(request);

        Product product = businessShared.getProductById(inventory.getProduct().getId(), inventory.getUser());

        throwIfTrue(product == null, 400, "Can't find product ID: " + inventory.getProduct().getId());

        inventory.setStatus(Status.ACTIVE.getValue());
        inventory.setProduct(product);

        inventory = super.save(inventory);

        return InventoryMapper.full(inventory);
    }

    /**
     * List inventories.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public List<InventoryDTO> list(Request request) {
        log.trace("");
        return this.findAll(request);
    }

    /**
     * List inventories.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public InventoryDTO listById(Request request) throws ServiceException {
        log.trace("");

        var inventory = this.getEntity(request);
        var optional = this.findById(inventory).map(InventoryMapper::full);

        throwIfTrue(optional.isEmpty(), 404, "Inventory not found for ID: " + request.id());

        return optional.get();

    }

    /**
     * Update inventory.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public InventoryDTO update(Request request) throws ServiceException {
        log.trace("");

        Inventory entity = this.getEntity(request);

        Product product = businessShared.getProductById(entity.getProduct().getId(), entity.getUser());
        throwIfTrue(product == null, 400, "Can't find product ID: " + entity.getProduct().getId());

        Optional<Inventory> optional = this.findById(entity);
        throwIfTrue(optional.isEmpty(), 404, "Inventory not found for ID: " + request.id());

        Inventory inventory = optional.get();
        inventory.setDescription(entity.getDescription());
        inventory.setQuantity(entity.getQuantity());
        inventory.setStatus(Status.ACTIVE.getValue());
        inventory.setProduct(product);

        super.update(inventory);

        return InventoryMapper.full(inventory);
    }

    /**
     * Delete inventory.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public InventoryDTO delete(Request request) {
        log.trace("");

        Inventory inventory = this.getEntity(request);
        super.delete(inventory);

        return null;
    }

    /**
     * Find all inventories.
     *
     * @param request {@linkplain Request}
     * @return list of {@linkplain InventoryDTO}
     */
    private List<InventoryDTO> findAll(Request request) {
        log.trace("");

        Inventory inventory = this.getEntity(request);
        Collection<Inventory> inventories = super.findAll(inventory);
        return inventories.stream().map(InventoryMapper::full).toList();
    }

    /**
     * Find inventory by ID.
     *
     * @param inventory
     * @return {@linkplain Optional} of {@linkplain Inventory}
     */
    private Optional<Inventory> findById(Inventory inventory) {
        inventory = super.find(inventory);
        return Optional.ofNullable(inventory);
    }

    public boolean hasInventory(Inventory inventory) {
        return this.getDAO().has(inventory);
    }
}