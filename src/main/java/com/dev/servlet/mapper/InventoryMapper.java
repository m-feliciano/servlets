package com.dev.servlet.mapper;

import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.pojo.Inventory;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class InventoryMapper extends BaseMapper<Inventory, InventoryDTO> {

    /**
     * {@link Inventory} from {@link InventoryDTO}
     *
     * @param dto {@link InventoryDTO}
     * @return {@link Inventory}
     */
    public static Inventory from(InventoryDTO dto) {
        Inventory inventory = new Inventory();
        inventory.setId(dto.getId());
        inventory.setQuantity(dto.getQuantity());
        inventory.setStatus(dto.getStatus());
        inventory.setDescription(dto.getDescription());
        inventory.setProduct(ProductMapper.from(dto.getProduct()));
        inventory.setUser(UserMapper.from(dto.getUser()));
        return inventory;
    }

    /**
     * {@link InventoryDTO} from {@link Inventory}
     *
     * @param inventory {@link Inventory}
     * @return {@link InventoryDTO}
     */
    public static InventoryDTO from(Inventory inventory) {
        if (inventory == null) return null;

        return InventoryDTO.builder()
                .id(inventory.getId())
                .quantity(inventory.getQuantity())
                .status(inventory.getStatus())
                .description(inventory.getDescription())
                .product(ProductMapper.from(inventory.getProduct()))
                .user(UserMapper.onlyId(inventory.getUser()))
                .build();
    }

    @Override
    public InventoryDTO fromEntity(Inventory object) {
        return from(object);
    }

    @Override
    public Inventory toEntity(InventoryDTO object) {
        return from(object);
    }
}
