package com.dev.servlet.domain.service.internal;
import com.dev.servlet.domain.service.IBusinessService;
import com.dev.servlet.domain.transfer.dto.InventoryDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.InventoryMapper;
import com.dev.servlet.domain.model.Category;
import com.dev.servlet.domain.model.Inventory;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.User;
import com.dev.servlet.domain.model.enums.Status;
import com.dev.servlet.domain.service.IStockService;
import com.dev.servlet.infrastructure.persistence.dao.InventoryDAO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import static com.dev.servlet.core.util.CryptoUtils.getUser;
import static com.dev.servlet.core.util.ThrowableUtils.throwServiceError;

@Slf4j
@NoArgsConstructor
@Model
public class StockServiceImpl extends BaseServiceImpl<Inventory, Long> implements IStockService {
    @Inject
    private IBusinessService businessService;
    @Inject
    public StockServiceImpl(InventoryDAO dao) {
        super(dao);
    }

    private InventoryDAO getDAO() {
        return (InventoryDAO) super.getBaseDAO();
    }
    @Override
    public Class<InventoryDTO> getDataMapper() {
        return InventoryDTO.class;
    }
    @Override
    public Inventory toEntity(Object object) {
        return InventoryMapper.full((InventoryDTO) object);
    }

    public Inventory getEntity(Request request) {
        log.trace("");
        Inventory inventory = requestBody(request.getBody());
        inventory = Optional.ofNullable(inventory).orElse(new Inventory());
        String parameter = request.getParameter("productId");
        if (parameter != null) {
            Long productId = Long.parseLong(parameter);
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
        inventory.setUser(getUser(request.getToken()));
        return inventory;
    }
    @Override
    public InventoryDTO create(Request request) throws ServiceException {
        log.trace("");
        Inventory inventory = this.getEntity(request);
        Product product = businessService.getProductById(inventory.getProduct().getId(), inventory.getUser());
        if (product == null) {
            throwServiceError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
        inventory.setStatus(Status.ACTIVE.getValue());
        inventory.setProduct(product);
        inventory = super.save(inventory);
        return InventoryMapper.full(inventory);
    }
    @Override
    public List<InventoryDTO> list(Request request) {
        log.trace("");
        return this.findAll(request);
    }
    @Override
    public InventoryDTO findById(Request request) throws ServiceException {
        log.trace("");
        Inventory entity = this.getEntity(request);
        Inventory inventory = this.findById(entity);
        return InventoryMapper.full(inventory);
    }
    @Override
    public InventoryDTO update(Request request) throws ServiceException {
        log.trace("");
        Inventory entity = this.getEntity(request);
        Product product = businessService.getProductById(entity.getProduct().getId(), entity.getUser());
        if (product == null) {
            throwServiceError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
        Inventory inventory = this.findById(entity);
        inventory.setDescription(entity.getDescription());
        inventory.setQuantity(entity.getQuantity());
        inventory.setStatus(Status.ACTIVE.getValue());
        inventory.setProduct(product);
        super.update(inventory);
        return InventoryMapper.full(inventory);
    }
    @Override
    public boolean delete(Request request) throws ServiceException {
        log.trace("");
        Inventory inventory = this.findById(this.getEntity(request));
        return super.delete(inventory);
    }

    public boolean hasInventory(Inventory inventory) {
        return this.getDAO().has(inventory);
    }

    private List<InventoryDTO> findAll(Request request) {
        log.trace("");
        Inventory inventory = this.getEntity(request);
        Collection<Inventory> inventories = super.findAll(inventory);
        return inventories.stream().map(InventoryMapper::full).toList();
    }

    private Inventory findById(Inventory filter) throws ServiceException {
        Inventory inventory = super.find(
                Inventory.builder()
                        .id(filter.getId())
                        .user(new User(filter.getUser().getId()))
                        .build()
        );
        if (inventory == null) {
            throwServiceError(HttpServletResponse.SC_NOT_FOUND, "Inventory not found");
        }
        return inventory;
    }
}
