package com.dev.servlet.domain.transfer.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO extends DataTransferObject<Long> {
    private Long id;
    private Integer quantity;
    private String description;
    private ProductDTO product;
    private String status;
    private UserDTO user;
}
