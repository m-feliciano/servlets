package com.dev.servlet.domain.service;


import com.dev.servlet.application.transfer.dto.CategoryDTO;
import com.dev.servlet.application.transfer.request.Request;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.CategoryMapper;
import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.core.util.CollectionUtils;
import com.dev.servlet.domain.model.pojo.domain.Category;
import com.dev.servlet.domain.model.pojo.enums.Status;
import com.dev.servlet.infrastructure.persistence.dao.CategoryDAO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.dev.servlet.core.util.ThrowableUtils.throwIfTrue;

/**
 * Category Service.
 *
 * @see BaseService
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Model
public class CategoryService extends BaseService<Category, Long> {

    public static final String NAME = "name";
    private static final String CACHE_KEY = "categoryCacheKey";

    @Inject
    public CategoryService(CategoryDAO categoryDAO) {
        super(categoryDAO);
    }

    @Override
    protected Class<CategoryDTO> getTransferClass() {
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
        category = super.save(category);

        CacheUtils.clear(CACHE_KEY, request.token());

        return CategoryMapper.from(category);
    }

    /**
     * Update an existing category.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponse}
     * @throws ServiceException if an error occurs during update
     */
    public CategoryDTO update(Request request) throws ServiceException {
        log.trace("");

        Optional<Category> optional = this.findById(request.id(), request.token());

        throwIfTrue(optional.isEmpty(), 404, "Category not found for ID: " + request.id());

        Category category = optional.get();
        category.setName(request.getParameter(NAME).toUpperCase());
        super.update(category);

        CacheUtils.clear(CACHE_KEY, request.token());

        return CategoryMapper.from(category);
    }

    /**
     * List categories or a specific category.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponse}
     */
    public CategoryDTO listById(Request request) throws ServiceException {
        log.trace("");

        Optional<CategoryDTO> optional = this.findById(request.id(), request.token())
                .map(CategoryMapper::from);

        throwIfTrue(optional.isEmpty(), 404, "Category not found for ID: " + request.id());

        return optional.get();
    }


    /**
     * List categories or a specific category.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponse}
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

        Optional<Category> optional = this.findById(request.id(), request.token());
        throwIfTrue(optional.isEmpty(), 404, "Category not found for ID: " + request.id());

        Category category = optional.get();
        super.delete(category);
        CacheUtils.clear(CACHE_KEY, request.token());

        return CategoryMapper.from(category);
    }

    /**
     * Get all categories from cache.
     *
     * @param token the user token
     * @return the list of categories
     */
    public Collection<CategoryDTO> getAllFromCache(String token) {
        List<CategoryDTO> dtoList = CacheUtils.get(CACHE_KEY, token);

        if (CollectionUtils.isEmpty(dtoList)) {
            Category category = new Category(super.getUser(token));
            var categories = super.findAll(category);

            if (!CollectionUtils.isEmpty(categories)) {
                dtoList = categories.stream().map(CategoryMapper::from).toList();
                CacheUtils.set(CACHE_KEY, token, dtoList);
            }
        }
//
        return dtoList;
    }

    /**
     * Find a category by ID.
     *
     * @param resourceId the category ID
     * @param token      the user token
     * @return the category DTO
     */
    public Optional<Category> findById(Object resourceId, String token) {
        Collection<CategoryDTO> categories = this.getAllFromCache(token);

        Long id = resourceId == null ? null : Long.valueOf(resourceId.toString());

        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(CategoryMapper::from);
    }
}