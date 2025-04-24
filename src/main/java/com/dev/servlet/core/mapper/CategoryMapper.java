package com.dev.servlet.core.mapper;
import com.dev.servlet.domain.transfer.dto.CategoryDTO;
import com.dev.servlet.domain.model.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    public static CategoryDTO from(Category category) {
        if (category == null) return null;
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .status(category.getStatus())
                .user(UserMapper.onlyId(category.getUser()))
                .build();
    }

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
