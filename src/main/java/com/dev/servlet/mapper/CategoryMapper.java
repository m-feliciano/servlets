package com.dev.servlet.mapper;

import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.pojo.Category;

public final class CategoryMapper {

    private CategoryMapper() {
        throw new IllegalStateException("CategoryMapper class");
    }

    /**
     * {@link CategoryDTO} from {@link Category}
     *
     * @param category {@link Category}
     * @return {@link CategoryDTO}
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
     * {@link Category} from {@link CategoryDTO}
     *
     * @param dto {@link CategoryDTO}
     * @return {@link Category}
     */
    public static Category from(CategoryDTO dto) {
        if (dto == null) return null;
        Category category = new Category(dto.getId());
        category.setName(dto.getName());
        category.setUser(UserMapper.full(dto.getUser()));
        category.setStatus(dto.getStatus());
        if (dto.getProducts() != null) {
            category.setProducts(dto.getProducts().stream().map(ProductMapper::full).toList());
        }
        return category;
    }

    public static CategoryDTO onlyId(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder().id(category.getId()).name(category.getName()).build();
    }
}
