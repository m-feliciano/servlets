package com.dev.servlet.core.mapper;

import com.dev.servlet.application.dto.CategoryDTO;
import com.dev.servlet.domain.model.pojo.domain.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    /**
     * {@linkplain CategoryDTO} from {@linkplain Category}
     *
     * @param category {@linkplain Category}
     * @return {@linkplain CategoryDTO}
     */
    public static CategoryDTO from(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .status(category.getStatus())
                .user(UserMapper.onlyId(category.getUser()))
                .build();
    }

    /**
     * {@linkplain Category} from {@linkplain CategoryDTO}
     *
     * @param dto {@linkplain CategoryDTO}
     * @return {@linkplain Category}
     */
    public static Category from(CategoryDTO dto) {
        if (dto == null) return null;
        Category category = new Category(dto.getId());
        category.setName(dto.getName());
        category.setUser(UserMapper.onlyId(dto.getUser()));
        category.setStatus(dto.getStatus());
        if (dto.getProducts() != null) {
            category.setProducts(dto.getProducts().stream().map(ProductMapper::base).toList());
        }
        return category;
    }

    public static CategoryDTO onlyId(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder().id(category.getId()).name(category.getName()).build();
    }
}

