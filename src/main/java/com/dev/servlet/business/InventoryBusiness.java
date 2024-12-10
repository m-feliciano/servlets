package com.dev.servlet.business;

import com.dev.servlet.business.shared.BusinessShared;
import com.dev.servlet.controllers.InventoryController;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.interfaces.ResourceMapping;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.mapper.InventoryMapper;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.Inventory;
import com.dev.servlet.pojo.Product;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Inventory business.
 * <p>
 * This class is responsible for handling the inventory business logic.
 *
 * @see BaseBusiness
 * @since 1.0
 */
@Setter
@NoArgsConstructor
@Singleton
@ResourcePath("inventory")
public class InventoryBusiness extends BaseBusiness<Inventory, Long, InventoryDTO> {

    @Inject
    private CategoryBusiness categoryBusiness;
    @Inject
    private BusinessShared businessShared;

    @Inject
    public InventoryBusiness(InventoryController controller) {
        super(controller);
        super.setResponseEntity("item"); // if the name of the entity is different from the path
        this.mapper = new InventoryMapper();
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

    @ResourceMapping(NEW)
    public Response forwardRegister() {
        LOGGER.trace("");

        return super.createResponse(HttpServletResponse.SC_FOUND, null, super.forwardTo("formCreateItem"));
    }


    @ResourceMapping(CREATE)
    public Response create(Request request, String token) throws ServiceException {
        LOGGER.trace("");

        User user = getUser(token);

        Inventory inventory = this.getEntity(request);

        Product product = businessShared.getProductById(inventory.getProduct().getId());
        if (product == null || !user.getId().equals(product.getUser().getId())) {
            throw new ServiceException("Product #" + inventory.getProduct().getId() + " was not found");
        }

        inventory.setStatus(StatusEnum.ACTIVE.getValue());
        inventory.setProduct(product);
        inventory.setUser(user);

        super.save(inventory);

        return super.createResponse(HttpServletResponse.SC_CREATED, inventory, super.redirectTo(inventory.getId()));
    }

    /**
     * List inventories.
     *
     * @param request {@link Request}
     * @param token
     * @return {@link Response}
     */
    @ResourceMapping(LIST)
    public Response list(Request request, String token) {
        LOGGER.trace("");

        if (request.getEntityId() != null) {
            long id = Long.parseLong(request.getEntityId());

            Optional<Response> optional = findById(id, token)
                    .map(i -> createResponse(HttpServletResponse.SC_OK, i, super.forwardTo("formListItem")));

            return optional.orElseGet(() -> super.responseEntityNotFound(id));
        }

        List<InventoryDTO> inventories = this.findAll(request, token);
        List<CategoryDTO> categories = categoryBusiness.getAllFromCache(token);

        var responseData = new Response.Data()
                .add("items", inventories)
                .add("categories", categories);

        return Response.of(responseData).next(super.forwardTo("listItems"));
    }

    /**
     * Update inventory.
     *
     * @param request {@link Request}
     * @param token
     * @return {@link Response}
     */
    @ResourceMapping(UPDATE)
    public Response update(Request request, String token) {
        LOGGER.trace("");

        Long id = Long.parseLong(request.getEntityId());

        Optional<Inventory> optional = this.findById(id, token);
        if (optional.isEmpty()) {
            return super.responseEntityNotFound(id);
        }

        Inventory inventoryRequest = this.getEntity(request);
        User user = getUser(token);

        Product product = businessShared.getProductById(inventoryRequest.getProduct().getId());
        if (product == null || !product.getUser().getId().equals(user.getId())) {
            return super.responseEntityNotFound(inventoryRequest.getProduct().getId());
        }

        Inventory inventory = optional.get();
        inventory.setDescription(inventoryRequest.getDescription());
        inventory.setQuantity(inventoryRequest.getQuantity());
        inventory.setStatus(StatusEnum.ACTIVE.getValue());
        inventory.setProduct(product);
        inventory.setUser(user);

        super.update(inventory);

        return super.createResponse(HttpServletResponse.SC_OK, inventory, super.redirectTo(id));
    }

    /**
     * Edit inventory.
     *
     * @param request {@link Request}
     * @param token
     * @return {@link Response}
     */
    @ResourceMapping(EDIT)
    public Response edit(Request request, String token) {
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());
        Optional<Response> optional = this.findById(id, token)
                .map(ivt -> createResponse(HttpServletResponse.SC_OK, ivt, super.forwardTo("formUpdateItem")));

        return optional.orElseGet(() -> super.responseEntityNotFound(id));
    }

    /**
     * Delete inventory.
     *
     * @param token
     * @param request {@link Request}
     * @return {@link Response}
     */
    @ResourceMapping(DELETE)
    public Response delete(String token, Request request) {
        LOGGER.trace("");

        Inventory inventory = this.getEntity(request);
        inventory.setUser(getUser(token));
        super.delete(inventory);

        return super.createResponse(HttpServletResponse.SC_NO_CONTENT, null, super.redirectTo(LIST));
    }

    /**
     * Find all inventories.
     *
     * @param request {@link Request}
     * @param token
     * @return list of {@link InventoryDTO}
     */
    private List<InventoryDTO> findAll(Request request, String token) {
        LOGGER.trace("");

        Inventory inventory = this.getEntity(request);
        inventory.setUser(getUser(token));
        Collection<Inventory> inventories = super.findAll(inventory);
        return inventories.stream().map(super::fromEntity).toList();
    }

    /**
     * Find inventory by ID.
     *
     * @param id
     * @param token
     * @return {@link Optional} of {@link Inventory}
     */
    private Optional<Inventory> findById(Long id, String token) {
        if (id == null) return Optional.empty();

        Inventory inventory = new Inventory(id);
        inventory.setUser(getUser(token));
        inventory = super.find(inventory);
        return Optional.ofNullable(inventory);
    }
}