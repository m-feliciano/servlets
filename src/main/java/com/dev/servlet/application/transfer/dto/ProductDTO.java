package com.dev.servlet.application.transfer.dto;

import com.dev.servlet.domain.model.pojo.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO for {@linkplain Product}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO extends DataTransferObject<Long> {
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

