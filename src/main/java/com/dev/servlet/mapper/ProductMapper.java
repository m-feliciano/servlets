package com.dev.servlet.mapper;

import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.pojo.Product;

public final class ProductMapper {

    private ProductMapper() {
        throw new IllegalStateException("ProductMapper class");
    }

    /**
     * {@link ProductDTO} from {@link Product}
     *
     * @param product {@link Product}
     * @return {@link ProductDTO}
     */
    public static ProductDTO full(Product product) {
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
     * {@link ProductDTO} from {@link Product}
     *
     * @param product {@link Product}
     * @return {@link ProductDTO}
     */
    public static ProductDTO base(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .status(product.getStatus())
                .registerDate(product.getRegisterDate())
                .url(product.getUrl())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }

    /**
     * {@link Product} from {@link ProductDTO}
     *
     * @param dto {@link ProductDTO}
     * @return {@link Product}
     */
    public static Product full(ProductDTO dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setStatus(dto.getStatus());
        product.setRegisterDate(dto.getRegisterDate());
        product.setUrl(dto.getUrl());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setUser(UserMapper.full(dto.getUser()));
        product.setCategory(CategoryMapper.from(dto.getCategory()));
        return product;
    }
}
