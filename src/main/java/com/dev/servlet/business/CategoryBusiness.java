package com.dev.servlet.business;

import com.dev.servlet.controllers.CategoryController;
import com.dev.servlet.dto.CategoryDTO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.interfaces.ResourceMapping;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.mapper.CategoryMapper;
import com.dev.servlet.pojo.Category;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import com.dev.servlet.utils.CacheUtil;
import com.dev.servlet.utils.CollectionUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * Category Business.
 * <p>
 * This class is responsible for handling the category business logic.
 *
 * @see BaseBusiness
 * @since 1.0
 */
@Setter
@NoArgsConstructor
@Singleton
@ResourcePath("category")
public class CategoryBusiness extends BaseBusiness<Category, Long, CategoryDTO> {

    private static final String CACHE_KEY = "categoryCacheKey";

    @Inject
    public CategoryBusiness(CategoryController controller) {
        super(controller);
        this.mapper = new CategoryMapper();
    }

    /**
     * Forward to the register form.
     *
     * @return the response with the next path
     */
    @ResourceMapping(NEW)
    public Response forwardRegister() {
        LOGGER.trace("");

        Response response = new Response(HttpServletResponse.SC_FOUND);
        return response.next(super.forwardTo("formCreateCategory"));
    }

    /**
     * Create a new category.
     *
     * @param request the request containing the category details
     * @param token   the user token
     * @return the response with the next path
     * @throws ServiceException if an error occurs during creation
     */
    @ResourceMapping(CREATE)
    public Response registerOne(Request request, String token) throws ServiceException {
        LOGGER.trace("");

        Category category = this.getEntity(request);
        category.setUser(getUser(token));
        category.setStatus(StatusEnum.ACTIVE.getValue());
        super.save(category);

        CacheUtil.clear(CACHE_KEY, token);

        return super.createResponse(HttpServletResponse.SC_CREATED, category, super.redirectTo(category.getId()));
    }

    /**
     * Update an existing category.
     *
     * @param request {@link Request}
     * @param token
     * @return {@link Response}
     * @throws ServiceException if an error occurs during update
     */
    @ResourceMapping(UPDATE)
    public Response update(Request request, String token) throws ServiceException {
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());
        Optional<Category> optional = this.findById(id, token);
        if (optional.isEmpty()) {
            return super.responseEntityNotFound(id);
        }

        Category category = optional.get();
        category.setName(request.getRequiredParameter("name").toUpperCase());
        super.update(category);

        CacheUtil.clear(CACHE_KEY, token);

        return super.createResponse(HttpServletResponse.SC_OK, category, super.redirectTo(category.getId()));
    }

    /**
     * List categories or a specific category.
     *
     * @param request {@link Request}
     * @param token
     * @return {@link Response}
     */
    @ResourceMapping(LIST)
    public Response list(Request request, String token) {
        LOGGER.trace("");

        if (request.getEntityId() != null) {
            long id = Long.parseLong(request.getEntityId());

            String forward = forwardTo("formListCategory");
            Optional<Response> optional = this.findById(id, token)
                    .map(c -> createResponse(HttpServletResponse.SC_OK, c, forward));

            return optional.orElseGet(() -> super.responseEntityNotFound(id));
        }

        List<CategoryDTO> categories = getAllFromCache(token);
        String parameter = request.getParameter("name");
        if (parameter != null) {
            categories = categories.stream()
                    .filter(c -> c.getName().toLowerCase().contains(parameter.toLowerCase()))
                    .toList();
        }

        Response response = Response.of(Response.Data.of("categories", categories));

        return response.next(super.forwardTo("listCategories"));
    }

    /**
     * Edit an existing category.
     *
     * @param request the request containing the category ID
     * @param token   the user token
     * @return the response with the next path
     */
    @ResourceMapping(EDIT)
    public Response edit(Request request, String token) {
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());
        Optional<Category> optional = this.findById(id, token);

        Optional<Response> response = optional.map(
                c -> createResponse(HttpServletResponse.SC_OK, c, super.forwardTo("formUpdateCategory")));

        return response.orElseGet(() -> super.responseEntityNotFound(id));

    }

    /**
     * Delete an existing category.
     *
     * @param request the request containing the category ID
     * @param token   the user token
     * @return the response with the next path
     * @throws ServiceException if an error occurs during deletion
     */
    @ResourceMapping(DELETE)
    public Response delete(Request request, String token) throws ServiceException {
        LOGGER.trace("");

        long id = Long.parseLong(request.getEntityId());

        Optional<Category> optional = this.findById(id, token);
        if (optional.isEmpty()) {
            return super.responseEntityNotFound(id);
        }

        Category category = optional.get();
        super.delete(category);
        CacheUtil.clear(CACHE_KEY, token);

        Response response = new Response(HttpServletResponse.SC_NO_CONTENT);
        return response.next(redirectTo(LIST));
    }

    /**
     * Get all categories from cache.
     *
     * @param token the user token
     * @return the list of categories
     */
    public List<CategoryDTO> getAllFromCache(String token) {
        List<CategoryDTO> dtoList = CacheUtil.get(CACHE_KEY, token);

        if (CollectionUtils.isEmpty(dtoList)) {
            Category category = new Category();
            category.setUser(getUser(token));
            var categories = super.findAll(category);

            if (!CollectionUtils.isEmpty(categories)) {
                dtoList = categories.stream().map(super::fromEntity).toList();
                CacheUtil.set(CACHE_KEY, token, dtoList);
            }
        }

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

        List<CategoryDTO> categories = getAllFromCache(token);

        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(super::toEntity);
    }
}