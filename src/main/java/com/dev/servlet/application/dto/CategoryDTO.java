package com.dev.servlet.application.dto;

import com.dev.servlet.domain.model.pojo.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO for {@linkplain Category}
 */
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

