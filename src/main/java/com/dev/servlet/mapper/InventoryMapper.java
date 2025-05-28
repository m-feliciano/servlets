package com.dev.servlet.mapper;

import com.dev.servlet.dto.InventoryDTO;
import com.dev.servlet.model.pojo.domain.Inventory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryMapper {
    /**
     * {@linkplain Inventory} from {@linkplain InventoryDTO}
     *
     * @param dto {@linkplain InventoryDTO}
     * @return {@linkplain Inventory}
     */
    public static Inventory full(InventoryDTO dto) {
        Inventory inventory = new Inventory();
        inventory.setId(dto.getId());
        inventory.setQuantity(dto.getQuantity());
        inventory.setStatus(dto.getStatus());
        inventory.setDescription(dto.getDescription());
        inventory.setProduct(ProductMapper.base(dto.getProduct()));
        inventory.setUser(UserMapper.onlyId(dto.getUser()));
        return inventory;
    }

    /**
     * {@linkplain InventoryDTO} from {@linkplain Inventory}
     *
     * @param inventory {@linkplain Inventory}
     * @return {@linkplain InventoryDTO}
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
