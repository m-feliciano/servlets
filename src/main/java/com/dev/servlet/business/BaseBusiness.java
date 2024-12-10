package com.dev.servlet.business;

import com.dev.servlet.controllers.BaseController;
import com.dev.servlet.interfaces.IPagination;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.mapper.BaseMapper;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.records.KeyPair;
import com.dev.servlet.pojo.records.Pagination;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import com.dev.servlet.utils.ClassUtil;
import com.dev.servlet.utils.CryptoUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Base Business
 * <p>
 * This layer is supposed to be the business layer, where we handle the request that the specializations will execute.
 *
 * @param <T> the entity extends {@link Identifier} of {@link K}
 * @param <K> the entity id
 * @param <J> the transfer object (DTO), extends {@link Identifier} of {@link K}
 * @author marcelo.feliciano
 * @since 1.0
 */
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
public abstract class BaseBusiness<T extends Identifier<K>, K, J extends Serializable & Identifier<K>> implements IPagination<T, K> {

    // Common strings
    protected static final String CREATE = "create"; // Create resource
    protected static final String LIST = "list";
    protected static final String UPDATE = "update";
    protected static final String DELETE = "delete";
    protected static final String NEW = "new"; // Forward to create
    protected static final String EDIT = "edit";
    protected static final String CATEGORY = "category";
    protected static final String NOT_FOUND = "Not found.";

    // Exported paths
    protected static final String FORWARD_PAGES_FORM_LOGIN = "forward:pages/formLogin.jsp";
    protected static final String FORWARD_CREATE_USER = "forward:pages/user/formCreateUser.jsp";

    // Common paths
    private static final String FORWARD_TO = "forward:pages/{webService}/{context}.jsp";
    private static final String REDIRECT_TO = "redirect:/view/{webService}/{context}";

    // Logger
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseBusiness.class);

    private BaseController<T, K> controller;
    protected BaseMapper<T, J> mapper;

    @Getter(AccessLevel.NONE)
    private String responseEntity;

    @Setter(AccessLevel.NONE)
    private String webService;

    protected BaseBusiness(BaseController<T, K> controller) {
        this.controller = controller;
        webService = this.getClass().getAnnotation(ResourcePath.class).value();
        this.setResponseEntity(webService);
    }

    protected User getUser(String token) {
        return CryptoUtils.getUser(token);
    }

    protected Response responseEntityNotFound(K id) {
        String capitalizedWebService = webService.substring(0, 1).toUpperCase() + webService.substring(1);

        return Response.ofError(HttpServletResponse.SC_NOT_FOUND, capitalizedWebService + " ID " + id);
    }

    /**
     * Redirect to the path
     *
     * @param page {@link String}
     * @return {@link String}
     */
    protected String redirectTo(String page) {
        return getNext(REDIRECT_TO, page);
    }

    /**
     * Redirect to list entity
     *
     * @param entityId {@link K}
     * @return {@link String}
     */
    protected String redirectTo(K entityId) {
        return redirectTo(LIST).concat("/" + entityId);
    }

    /**
     * Forward to the path
     *
     * @param page {@link String}
     * @return
     */
    protected String forwardTo(String page) {
        return getNext(FORWARD_TO, page);
    }

    /**
     * Get the next path
     *
     * @param webService {@link String}
     * @param context    {@link String}
     * @return {@link String}
     */
    private String getNext(String webService, String context) {
        String replace = webService.replace("{webService}", this.webService);
        replace = replace.replace("{context}", context);
        return replace;
    }

    /**
     * Create a response
     *
     * @param status {@link HttpServletResponse} status
     * @param entity {@link T}
     * @param path   the next path
     * @return {@link Response} may be used to chain the next path
     * @author marcelo.feliciano
     */
    protected Response createResponse(int status, T entity, String path) {
        Response response = new Response(status);
        J object = this.fromEntity(entity);
        response.data(this.responseEntity, object);
        return response.next(path);
    }

    /**
     * Find by id
     *
     * @param id {@link K}
     * @return {@link T}
     */
    protected T findById(K id) {
        return controller.findById(id);
    }

    /**
     * Find all
     *
     * @param entity {@link T}
     * @return {@link Collection} of {@link T}
     */
    protected Collection<T> findAll(T entity) {
        return controller.findAll(entity);
    }

    /**
     * Find Entity
     *
     * @param entity {@link T}
     * @return {@link T}
     */
    protected T find(T entity) {
        return controller.find(entity);
    }

    /**
     * Delete Entity
     *
     * @param entity {@link T}
     */
    protected void delete(T entity) {
        controller.delete(entity);
    }

    /**
     * Update Entity
     *
     * @param entity {@link T}
     * @return {@link T}
     */
    protected T update(T entity) {
        return controller.update(entity);
    }

    /**
     * Save Entity
     *
     * @param entity {@link T}
     */
    protected void save(T entity) {
        controller.save(entity);
    }

    protected T toEntity(J object) {
        return mapper.toEntity(object);
    }

    protected J fromEntity(T object) {
        return mapper.fromEntity(object);
    }

    @Override
    public Collection<K> findAllOnlyIds(T entity) {
        return controller.findAllOnlyIds(entity);
    }

    @Override
    public Collection<T> getAllPageable(Collection<K> ids, Pagination pagination) {
        return controller.getAllPageable(ids, pagination);
    }


    /**
     * Populate the fields of the object
     *
     * @param object {@link J} the transfer object
     * @param id     {@link String} the object id
     * @param parameters   {@link List} of {@link KeyPair}
     * @return {@link J} the transfer object
     * @author marcelo.feliciano
     */
    private J fillObjectData(J object, String id, List<KeyPair> parameters) {
        if (id != null) {
            Class<K> typeK = ClassUtil.extractType(this.getClass(), 2);
            K idObject = ClassUtil.castWrapper(typeK, id);
            object.setId(idObject);
        }

        ClassUtil.fillObject(object, parameters);
        return object;
    }

    /**
     * Retrieve the base transfer object from the request
     *
     * @param request {@link Request}
     * @return {@link T} the entity
     * @author marcelo.feliciano
     */
    protected J getTransferObject(Request request) {
        Class<J> clazzTypeJ = ClassUtil.extractType(this.getClass(), 3);
        Optional<J> optionalJ = ClassUtil.createInstance(clazzTypeJ);

        String entityId = request.getEntityId();
        List<KeyPair> parameters = request.getParameters();

        return optionalJ.map(entity -> fillObjectData(entity, entityId, parameters)).orElse(null);
    }

    /**
     * Retrieve the base entity from the request
     *
     * @param request {@link Request}
     * @return {@link T} the entity
     * @author marcelo.feliciano
     */
    protected T getEntity(Request request) {
        return Optional.ofNullable(getTransferObject(request))
                .map(this::toEntity)
                .orElse(null);
    }
}