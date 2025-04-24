package com.dev.servlet.core.mapper;

import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.enums.Status;
import com.dev.servlet.infrastructure.external.webscrape.transfer.ProductWebScrapeDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductMapper {
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

    public static Product fromWebScrapeDTO(ProductWebScrapeDTO webScrapeDTO) {
        if (webScrapeDTO == null) return null;
        Product product = new Product();
        product.setName(webScrapeDTO.getTitle());
        product.setPrice(BigDecimal.valueOf(webScrapeDTO.getPrice()));
        product.setDescription(webScrapeDTO.getDescription());
        product.setUrl(webScrapeDTO.getThumbnail());
        product.setRegisterDate(webScrapeDTO.getPublishDate());
        product.setStatus(Status.ACTIVE.getValue());
        return product;
    }
}
