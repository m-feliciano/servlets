package com.dev.servlet.domain.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO extends DataTransferObject<Long> {
    private Long id;
    private String name;
    private List<ProductDTO> products;
    private String status;
    private UserDTO user;
}
