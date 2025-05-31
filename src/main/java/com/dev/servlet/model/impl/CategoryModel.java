package com.dev.servlet.model.impl;

import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.TransferObject;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.CategoryMapper;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.domain.Category;
import com.dev.servlet.model.pojo.enums.Status;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.persistence.dao.CategoryDAO;
import com.dev.servlet.util.CacheUtil;
import com.dev.servlet.util.CollectionUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Category Model.
 *
 * @see BaseModel
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Model
public class CategoryModel extends BaseModel<Category, Long> {

    private static final String CACHE_KEY = "categoryCacheKey";
    public static final String NAME = "name";

    @Inject
    public CategoryModel(CategoryDAO categoryDAO) {
        super(categoryDAO);
    }

    @Override
    protected Class<? extends TransferObject<Long>> getTransferClass() {
        return CategoryDTO.class;
    }

    @Override
    protected Category toEntity(Object object) {
        return CategoryMapper.from((CategoryDTO) object);
    }

    @Override
    protected Category getEntity(Request request) {
        Category category = super.getEntity(request);
        if (category != null) {
            category.setUser(getUser(request.token()));
        }

        return category;
    }

    /**
     * Create a new category.
     *
     * @param request the request containing the category details
     * @return the response with the next path
     * @throws ServiceException if an error occurs during creation
     */
    public CategoryDTO register(Request request) throws ServiceException {
        log.trace("");

        Category category = this.getEntity(request);
        category.setStatus(Status.ACTIVE.getValue());
        super.save(category);

        CacheUtil.clear(CACHE_KEY, request.token());

        return CategoryMapper.from(category);
    }

    /**
     * Update an existing category.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     * @throws ServiceException if an error occurs during update
     */
    public CategoryDTO update(Request request) throws ServiceException {
        log.trace("");

        Optional<Category> optCategory = this.findById(request.id(), request.token());
        Category category = optCategory.orElseThrow(() -> new404NotFoundException(request.id()));

        category.setName(request.getParameter(NAME).toUpperCase());
        super.update(category);

        CacheUtil.clear(CACHE_KEY, request.token());

        return CategoryMapper.from(category);
    }

    /**
     * List categories or a specific category.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public CategoryDTO listById(Request request) throws ServiceException {
        log.trace("");

        var optCategoryDTO = this.findById(request.id(), request.token()).map(CategoryMapper::from);
        return optCategoryDTO.orElseThrow(() -> new404NotFoundException(request.id()));
    }


    /**
     * List categories or a specific category.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public Collection<CategoryDTO> list(Request request) {
        log.trace("");

        Collection<CategoryDTO> categories = getAllFromCache(request.token());
        String parameter = request.getParameter(NAME);
        if (parameter != null) {
            String lowerCase = parameter.toLowerCase();

            categories = categories.stream()
                    .filter(c -> c.getName().toLowerCase().contains(lowerCase))
                    .toList();
        }

        return categories;
    }

    /**
     * Delete a category.
     *
     * @param request the request containing the category ID
     * @return the category DTO
     * @throws ServiceException if occurs errors
     */
    public CategoryDTO delete(Request request) throws ServiceException {
        log.trace("");

        Optional<Category> optCategory = this.findById(request.id(), request.token());
        Category category = optCategory.orElseThrow(() -> new404NotFoundException(request.id()));

        super.delete(category);
        CacheUtil.clear(CACHE_KEY, request.token());

        return CategoryMapper.from(category);
    }

    /**
     * Get all categories from cache.
     *
     * @param token the user token
     * @return the list of categories
     */
    public Collection<CategoryDTO> getAllFromCache(String token) {
        List<CategoryDTO> dtoList = CacheUtil.get(CACHE_KEY, token);

        if (CollectionUtils.isEmpty(dtoList)) {
            Category category = new Category(super.getUser(token));
            var categories = super.findAll(category);

            if (!CollectionUtils.isEmpty(categories)) {
                dtoList = categories.stream().map(CategoryMapper::from).toList();
                CacheUtil.set(CACHE_KEY, token, dtoList);
            }
        }
//
        return dtoList;
    }

    /**
     * Find a category by ID.
     *
     * @param recourceId the category ID
     * @param token    the user token
     * @return the category DTO
     */
    public Optional<Category> findById(Object recourceId, String token) {
        Collection<CategoryDTO> categories = this.getAllFromCache(token);

        Long id = recourceId == null ? null : Long.valueOf(recourceId.toString());

        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(CategoryMapper::from);
    }
}