package com.dev.servlet.dto;

import com.dev.servlet.pojo.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO for {@link com.dev.servlet.pojo.Inventory}
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO implements Identifier<Long>, Serializable {
    private Long id;
    private Integer quantity;
    private String description;
    private ProductDTO product;
    private String status;
    private UserDTO user;
}
