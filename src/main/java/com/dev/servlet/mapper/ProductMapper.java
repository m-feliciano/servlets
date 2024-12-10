package com.dev.servlet.mapper;

import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.pojo.Product;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ProductMapper extends BaseMapper<Product, ProductDTO> {

    /**
     * {@link ProductDTO} from {@link Product}
     *
     * @param product {@link Product}
     * @return {@link ProductDTO}
     */
    public static ProductDTO from(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .status(product.getStatus())
                .registerDate(product.getRegisterDate())
                .url(product.getUrl())
                .price(product.getPrice())
                .description(product.getDescription())
                .user(UserMapper.onlyId(product.getUser()))
                .category(CategoryMapper.onlyId(product.getCategory()))
                .build();
    }

    /**
     * {@link Product} from {@link ProductDTO}
     *
     * @param dto {@link ProductDTO}
     * @return {@link Product}
     */
    public static Product from(ProductDTO dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setStatus(dto.getStatus());
        product.setRegisterDate(dto.getRegisterDate());
        product.setUrl(dto.getUrl());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setUser(UserMapper.from(dto.getUser()));
        product.setCategory(CategoryMapper.from(dto.getCategory()));
        return product;
    }

    @Override
    public ProductDTO fromEntity(Product object) {
        return from(object);
    }

    @Override
    public Product toEntity(ProductDTO object) {
        return from(object);
    }
}
