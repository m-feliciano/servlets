package com.dev.servlet.dto;

import com.dev.servlet.model.pojo.domain.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for {@linkplain Inventory}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO extends TransferObject<Long> {
    private Long id;
    private Integer quantity;
    private String description;
    private ProductDTO product;
    private String status;
    private UserDTO user;
}
