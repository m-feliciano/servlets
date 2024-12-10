package com.dev.servlet.model;

import com.dev.servlet.dao.CategoryDAO;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.mapper.CategoryMapper;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.CacheUtil;
import com.dev.servlet.utils.CollectionUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Getter
@Setter
@NoArgsConstructor
@Model
public class CategoryModel extends BaseModel<Category, Long> {

    private static final String CACHE_KEY = "categoryCacheKey";
    public static final String NOT_FOUND = "Category #%s not found";

    @Inject
    public CategoryModel(CategoryDAO categoryDAO) {
        super(categoryDAO);
    }

    @Override
    protected Class<? extends Identifier<Long>> getTransferClass() {
        return CategoryDTO.class;
    }

    @Override
    protected Category toEntity(Object object) {
        return CategoryMapper.from((CategoryDTO) object);
    }

    /**
     * Create a new category.
     *
     * @param request the request containing the category details
     * @return the response with the next path
     * @throws ServiceException if an error occurs during creation
     */
    public CategoryDTO register(Request request) throws ServiceException {
        LOGGER.trace("");

        Category category = super.getEntity(request);
        category.setUser(getUser(request.getToken()));
        category.setStatus(StatusEnum.ACTIVE.getValue());
        super.save(category);

        CacheUtil.clear(CACHE_KEY, request.getToken());

        return CategoryMapper.from(category);
    }

    /**
     * Update an existing category.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     * @throws ServiceException if an error occurs during update
     */
    public CategoryDTO update(Request request) throws ServiceException {
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());

        Category category = this.findById(id, request.getToken())
                .orElseThrow(() -> new ServiceException(404, NOT_FOUND.formatted(id)));

        category.setName(request.getRequiredParameter("name").toUpperCase());
        super.update(category);

        CacheUtil.clear(CACHE_KEY, request.getToken());

        return CategoryMapper.from(category);
    }

    /**
     * List categories or a specific category.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public CategoryDTO listById(Request request) throws ServiceException {
        LOGGER.trace("");

        Long entityId = Long.parseLong(request.getEntityId());

        return this.findById(entityId, request.getToken())
                .map(CategoryMapper::from)
                .orElseThrow(() -> new ServiceException(404, NOT_FOUND.formatted(request.getEntityId())));
    }


    /**
     * List categories or a specific category.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public Collection<CategoryDTO> list(Request request) {
        LOGGER.trace("");

        Collection<CategoryDTO> categories = getAllFromCache(request.getToken());
        String parameter = request.getParameter("name");
        if (parameter != null) {
            categories = categories.stream()
                    .filter(c -> c.getName().toLowerCase().contains(parameter.toLowerCase()))
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
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());

        Optional<Category> optional = this.findById(id, request.getToken());
        if (optional.isEmpty()) {
            throw new ServiceException(404, NOT_FOUND.formatted(id));
        }

        Category category = optional.get();
        super.delete(category);
        CacheUtil.clear(CACHE_KEY, request.getToken());

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
            Category category = new Category();
            category.setUser(super.getUser(token));
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
     * @param id    the category ID
     * @param token the user token
     * @return the category DTO
     */
    public Optional<Category> findById(Long id, String token) {
        if (id == null) return Optional.empty();

        Collection<CategoryDTO> categories = this.getAllFromCache(token);

        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(CategoryMapper::from);
    }
}