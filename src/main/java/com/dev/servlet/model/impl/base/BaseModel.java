package com.dev.servlet.model.impl.base;

import com.dev.servlet.dto.TransferObject;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.ICrudRepository;
import com.dev.servlet.model.Identifier;
import com.dev.servlet.model.Mapper;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.persistence.IPageRequest;
import com.dev.servlet.persistence.IPageable;
import com.dev.servlet.persistence.dao.base.BaseDAO;
import com.dev.servlet.persistence.impl.PageableImpl;
import com.dev.servlet.util.ClassUtil;
import com.dev.servlet.util.CryptoUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Base Business
 * <p>
 * This layer is supposed to be the business layer, where we handle the request that the specializations will execute.
 *
 * @param <T> the entity extends {@linkplain Identifier} of {@linkplain K}
 * @param <K> the entity id
 * @implNote You should extend this class and provide a DAO specialization, which extends {@linkplain BaseDAO}.
 * @see BaseDAO
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
@SuppressWarnings("unchecked")
public abstract class BaseModel<T extends Identifier<K>, K> implements ICrudRepository<T, K> {

    protected BaseDAO<T, K> baseDAO;

    protected BaseModel(BaseDAO<T, K> baseDAO) {
        this.baseDAO = baseDAO;
    }

    @Override
    public Collection<T> findAll(T object) {
        return baseDAO.findAll(object);
    }

    @Override
    public T find(T object) {
        return baseDAO.find(object);
    }

    @Override
    public T findById(K id) {
        return baseDAO.findById(id);
    }

    @Override
    public void save(T object) {
        baseDAO.save(object);
    }

    @Override
    public T update(T object) {
        return baseDAO.update(object);
    }

    @Override
    public void delete(T object) {
        baseDAO.delete(object);
    }

    public Collection<K> findAllOnlyIds(T object) {
        return baseDAO.findAllOnlyIds(object);
    }

    @Override
    public IPageable<T> getAllPageable(IPageRequest<T> pageRequest) {
        long totalCount = baseDAO.count(pageRequest);

        Iterable<T> resultSet =  Collections.emptyList();
        if (totalCount > pageRequest.getFirstResult()) {
            resultSet = baseDAO.getAllPageable(pageRequest);
        }

        return PageableImpl.<T>builder()
                .content(resultSet)
                .totalElements(totalCount)
                .currentPage(pageRequest.getInitialPage())
                .pageSize(pageRequest.getPageSize())
                .sort(pageRequest.getSort())
                .build();
    }

    /**
     * Get all results with pagination and map to another type
     *
     * @param pageRequest {@linkplain IPageRequest}
     * @param mapper      {@linkplain Mapper} to convert the entity to another type
     * @return {@linkplain IPageable} with the results mapped to another type
     */
    public <U> IPageable<U> getAllPageable(IPageRequest<T> pageRequest, Mapper<T, U> mapper) {

        IPageable<T> page = getAllPageable(pageRequest);

        var content = ((List<T>) page.getContent()).stream().map(mapper::map).toList();

        return PageableImpl.cloneOf(page, content);
    }

    /**
     * Retrieve the transfer class
     *
     * @return {@linkplain Class} of {@linkplain Identifier} type {@linkplain K}
     */
    protected abstract Class<? extends TransferObject<K>> getTransferClass();

    /**
     * Convert the object to the entity
     *
     * @param object the object to be converted
     * @return {@linkplain T} the entity
     */
    protected abstract <J> T toEntity(J object);

    protected User getUser(String token) {
        return token != null ? CryptoUtils.getUser(token) : null;
    }

    /**
     * Retrieve the base entity from the request
     *
     * @param request {@linkplain Request}
     * @return {@linkplain T} the entity
     * @author marcelo.feliciano
     */
    protected T getEntity(Request request) {
        TransferObject<K> object = getTransferObject(request);
        return Optional.ofNullable(object).map(this::toEntity).orElse(null);
    }

    /**
     * Retrieve the base transfer object from the request
     *
     * @param request {@linkplain Request}
     * @return {@linkplain T} the entity
     * @author marcelo.feliciano
     */
    protected <R extends TransferObject<K>> R getTransferObject(Request request) {
        var optional = ClassUtil.createInstance(getTransferClass());
        if (optional.isEmpty()) return null;

        TransferObject<K> object = optional.get();
        ClassUtil.fillObject(object, request.body());
        return (R) object;
    }

    /**
     * Create a new service exception
     *
     * @param statusCode the status code
     * @param id   the entity id
     * @return {@linkplain ServiceException}
     */
    protected ServiceException newServiceExceptionOf(int statusCode, Object id) {
        var className = ClassUtil.extractType(this.getClass(), 1).getSimpleName();
        return new ServiceException(statusCode, String.format("%s with id %s not found.", className, id));
    }

    /**
     * Create a new 404 service exception
     *
     * @param id the entity id
     * @return {@linkplain ServiceException}
     */
    protected ServiceException new404NotFoundException(Object id) {
        return newServiceExceptionOf(404, id);
    }
}