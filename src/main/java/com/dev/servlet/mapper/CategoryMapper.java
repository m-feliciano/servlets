package com.dev.servlet.mapper;

import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.pojo.Category;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CategoryMapper extends BaseMapper<Category, CategoryDTO> {

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
        category.setUser(UserMapper.from(dto.getUser()));
        category.setStatus(dto.getStatus());
        if (dto.getProducts() != null) {
            category.setProducts(dto.getProducts().stream().map(ProductMapper::from).toList());
        }
        return category;
    }

    public static CategoryDTO onlyId(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder().id(category.getId()).name(category.getName()).build();
    }

    @Override
    public CategoryDTO fromEntity(Category object) {
        return from(object);
    }

    @Override
    public Category toEntity(CategoryDTO object) {
        return from(object);
    }
}
