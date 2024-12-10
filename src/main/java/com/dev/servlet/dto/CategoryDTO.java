package com.dev.servlet.dto;

import com.dev.servlet.pojo.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.dev.servlet.pojo.Category}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Identifier<Long>, Serializable {
    private Long id;
    private String name;
    private List<ProductDTO> products;
    private String status;
    private UserDTO user;
}
