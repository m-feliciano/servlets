package com.dev.servlet.mapper;

import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.pojo.Inventory;

public final class InventoryMapper {

    private InventoryMapper() {
        throw new IllegalStateException("InventoryMapper class");
    }

    /**
     * {@link Inventory} from {@link InventoryDTO}
     *
     * @param dto {@link InventoryDTO}
     * @return {@link Inventory}
     */
    public static Inventory full(InventoryDTO dto) {
        Inventory inventory = new Inventory();
        inventory.setId(dto.getId());
        inventory.setQuantity(dto.getQuantity());
        inventory.setStatus(dto.getStatus());
        inventory.setDescription(dto.getDescription());
        inventory.setProduct(ProductMapper.full(dto.getProduct()));
        inventory.setUser(UserMapper.full(dto.getUser()));
        return inventory;
    }

    /**
     * {@link InventoryDTO} from {@link Inventory}
     *
     * @param inventory {@link Inventory}
     * @return {@link InventoryDTO}
     */
    public static InventoryDTO full(Inventory inventory) {
        if (inventory == null) return null;

        return InventoryDTO.builder()
                .id(inventory.getId())
                .quantity(inventory.getQuantity())
                .status(inventory.getStatus())
                .description(inventory.getDescription())
                .product(ProductMapper.base(inventory.getProduct()))
                .user(UserMapper.onlyId(inventory.getUser()))
                .build();
    }
}
