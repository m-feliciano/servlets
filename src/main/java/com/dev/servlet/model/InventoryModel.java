package com.dev.servlet.model;

import com.dev.servlet.dao.InventoryDAO;
import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.mapper.InventoryMapper;
import com.dev.servlet.model.shared.BusinessShared;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.Inventory;
import com.dev.servlet.pojo.Product;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Inventory business.
 * <p>
 * This class is responsible for handling the inventory business logic.
 *
 * @see BaseModel
 * @since 1.0
 */
@Setter
@NoArgsConstructor
@Model
public class InventoryModel extends BaseModel<Inventory, Long> {

    @Inject
    BusinessShared businessShared;

    @Inject
    public InventoryModel(InventoryDAO dao) {
        super(dao);
    }

    private InventoryDAO getDAO() {
        return (InventoryDAO) super.getBaseDAO();
    }

    @Override
    protected Class<? extends Identifier<Long>> getTransferClass() {
        return InventoryDTO.class;
    }

    @Override
    protected Inventory toEntity(Object object) {
        return InventoryMapper.full((InventoryDTO) object);
    }

    @Override
    protected Inventory getEntity(Request request) {
        LOGGER.trace("");

        Inventory inventory = super.getEntity(request);
        inventory = Optional.ofNullable(inventory).orElse(new Inventory());

        if (request.getParameter("productId") != null) {
            Long productId = Long.parseLong(request.getParameter("productId"));
            inventory.setProduct(new Product(productId));
        }

        if (request.getQuery().getType() != null && request.getQuery().getSearch() != null) {
            Product product = new Product();

            if ("product".equals(request.getQuery().getType())) {
                product.setId(Long.valueOf(request.getQuery().getSearch().trim()));
                inventory.setProduct(product);
            } else if ("name".equals(request.getQuery().getType())) {
                product.setName(request.getQuery().getSearch().trim());

                inventory.setProduct(product);
            } else {
                inventory.setDescription(request.getQuery().getSearch().trim());
            }

            String categoryId = request.getParameter("category");
            if (categoryId != null && !categoryId.isEmpty()) {
                product.setCategory(new Category(Long.valueOf(categoryId)));
            }
        }

        return inventory;
    }

    /**
     * Create inventory.
     *
     * @param request {@link Request}
     * @return {@link InventoryDTO} the inventory
     * @throws ServiceException if any error occurs
     */
    public InventoryDTO create(Request request) throws ServiceException {
        LOGGER.trace("");

        User user = getUser(request.getToken());

        Inventory inventory = this.getEntity(request);

        Product product = businessShared.getProductById(inventory.getProduct().getId());
        if (product == null || !user.getId().equals(product.getUser().getId())) {
            throw new ServiceException(404, "Product #" + inventory.getProduct().getId() + " was not found");
        }

        inventory.setStatus(StatusEnum.ACTIVE.getValue());
        inventory.setProduct(product);
        inventory.setUser(user);

        super.save(inventory);

        return InventoryMapper.full(inventory);
    }

    /**
     * List inventories.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public List<InventoryDTO> list(Request request) {
        LOGGER.trace("");
        return this.findAll(request);
    }

    /**
     * List inventories.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public InventoryDTO listById(Request request) throws ServiceException {
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());
        String token = request.getToken();

        return this.findById(id, token)
                .map(InventoryMapper::full)
                .orElseThrow(() -> new ServiceException(404, "Inventory #" + id + " was not found"));
    }

    /**
     * Update inventory.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public InventoryDTO update(Request request) throws ServiceException {
        LOGGER.trace("");

        Long id = Long.parseLong(request.getEntityId());

        Inventory inventoryRequest = this.getEntity(request);
        User user = getUser(request.getToken());

        Product product = businessShared.getProductById(inventoryRequest.getProduct().getId());
        if (product == null || !product.getUser().getId().equals(user.getId())) {
            throw new ServiceException("Product #" + inventoryRequest.getProduct().getId() + " was not found");
        }

        Inventory inventory = this.findById(id, request.getToken())
                .orElseThrow(() -> new ServiceException(404, "Inventory #" + id + " was not found"));

        inventory.setDescription(inventoryRequest.getDescription());
        inventory.setQuantity(inventoryRequest.getQuantity());
        inventory.setStatus(StatusEnum.ACTIVE.getValue());
        inventory.setProduct(product);
        inventory.setUser(user);

        super.update(inventory);

        return InventoryMapper.full(inventory);
    }

    /**
     * Delete inventory.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public InventoryDTO delete(Request request) {
        LOGGER.trace("");

        Inventory inventory = this.getEntity(request);
        inventory.setUser(getUser(request.getToken()));
        super.delete(inventory);

        return null;
    }

    /**
     * Find all inventories.
     *
     * @param request {@link Request}
     * @return list of {@link InventoryDTO}
     */
    private List<InventoryDTO> findAll(Request request) {
        LOGGER.trace("");

        Inventory inventory = this.getEntity(request);
        inventory.setUser(getUser(request.getToken()));
        Collection<Inventory> inventories = super.findAll(inventory);
        return inventories.stream().map(InventoryMapper::full).toList();
    }

    /**
     * Find inventory by ID.
     *
     * @param id the inventory ID
     * @return {@link Optional} of {@link Inventory}
     */
    private Optional<Inventory> findById(Long id, String token) {
        if (id == null) return Optional.empty();

        Inventory inventory = new Inventory(id);
        inventory.setUser(getUser(token));
        inventory = super.find(inventory);
        return Optional.ofNullable(inventory);
    }

    public boolean hasInventory(Inventory inventory) {
        return this.getDAO().has(inventory);
    }
}