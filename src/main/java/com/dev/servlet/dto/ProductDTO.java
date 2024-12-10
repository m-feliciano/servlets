package com.dev.servlet.dto;

import com.dev.servlet.pojo.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO for {@link com.dev.servlet.pojo.Product}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO implements Identifier<Long>, Serializable {
    private Long id;
    private String name;
    private String description;
    private String url;
    private Date registerDate;
    private BigDecimal price;
    private UserDTO user;
    private String status;
    private CategoryDTO category;
}
