package com.dev.servlet.mapper;

import com.dev.servlet.dto.ProductDTO;
import com.dev.servlet.model.pojo.domain.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductMapper {

    /**
     * {@linkplain ProductDTO} from {@linkplain Product}
     *
     * @param product {@linkplain Product}
     * @return {@linkplain ProductDTO}
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
     * {@linkplain ProductDTO} from {@linkplain Product}
     *
     * @param product {@linkplain Product}
     * @return {@linkplain ProductDTO}
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
     * {@linkplain ProductDTO} from {@linkplain Product}
     *
     * @param productDTO {@linkplain Product}
     * @return {@linkplain ProductDTO}
     */
    public static Product base(ProductDTO productDTO) {
        if (productDTO == null) return null;

        Product product = new Product();
        product.setId(product.getId());
        product.setName(product.getName());
        product.setStatus(product.getStatus());
        product.setRegisterDate(product.getRegisterDate());
        product.setUrl(product.getUrl());
        product.setPrice(product.getPrice());
        product.setDescription(product.getDescription());
        return product;
    }

    /**
     * {@linkplain Product} from {@linkplain ProductDTO}
     *
     * @param dto {@linkplain ProductDTO}
     * @return {@linkplain Product}
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
        product.setUser(UserMapper.onlyId(dto.getUser()));
        product.setCategory(CategoryMapper.from(dto.getCategory()));
        return product;
    }
}
