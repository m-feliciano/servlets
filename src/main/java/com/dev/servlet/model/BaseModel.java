package com.dev.servlet.model;

import com.dev.servlet.dao.BaseDAO;
import com.dev.servlet.interfaces.IModel;
import com.dev.servlet.interfaces.IPagination;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.records.KeyPair;
import com.dev.servlet.pojo.records.Pagination;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.ClassUtil;
import com.dev.servlet.utils.CryptoUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @implNote You should extend this class and provide a DAO specialization, which extends {@link BaseDAO}.
 * @see BaseDAO
 */
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
public abstract class BaseModel<T extends Identifier<K>, K> implements IModel<T, K>, IPagination<T, K> {

    // Logger
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseModel.class);

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

    @Override
    public Collection<K> findAllOnlyIds(T object) {
        return baseDAO.findAllOnlyIds(object);
    }

    @Override
    public Collection<T> getAllPageable(Collection<K> ids, Pagination pagination) {
        return baseDAO.getAllPageable(ids, pagination);
    }

    /**
     * Retrieve the transfer class
     *
     * @return {@link Class} of {@link Identifier} type {@link K}
     */
    protected abstract Class<? extends Identifier<K>> getTransferClass();

    /**
     * Convert the object to the entity
     *
     * @param object the object to be converted
     * @return {@link T} the entity
     */
    protected abstract T toEntity(Object object);

    protected User getUser(String token) {
        return CryptoUtils.getUser(token);
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

    /**
     * Retrieve the base transfer object from the request
     *
     * @param request {@link Request}
     * @return {@link T} the entity
     * @author marcelo.feliciano
     */
    protected Object getTransferObject(Request request) {
        Optional<? extends Identifier<K>> optional = ClassUtil.createInstance(getTransferClass());

        String entityId = request.getEntityId();
        List<KeyPair> parameters = request.getBody();

        return optional
                .map(entity -> fillObjectData(entity, entityId, parameters))
                .orElse(null);
    }

    /**
     * Convert the transfer object to the entity
     *
     * @param object     the transfer object {@link U}
     * @param id         the entity id
     * @param parameters {@link List} of {@link KeyPair}
     * @param <U>        the transfer object
     * @return {@link Identifier} of {@link K}-
     * @author marcelo.feliciano
     */
    private <U extends Identifier<K>> Identifier<K> fillObjectData(U object, String id, List<KeyPair> parameters) {
        if (id != null) {
            Class<K> typeK = ClassUtil.extractType(this.getClass(), 2);
            K objectK = ClassUtil.castWrapper(typeK, id);
            object.setId(objectK);
        }

        ClassUtil.fillObject(object, parameters);
        return object;
    }

}