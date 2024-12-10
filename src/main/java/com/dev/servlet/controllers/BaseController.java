package com.dev.servlet.controllers;

import com.dev.servlet.dao.BaseDAO;
import com.dev.servlet.interfaces.IController;
import com.dev.servlet.pojo.records.Pagination;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseController<T, J> implements IController<T, J> {

    @Getter(value = AccessLevel.PROTECTED)
    private BaseDAO<T, J> baseDAO;

    @Override
    public T find(T object) {
        return baseDAO.find(object);
    }

    public T findById(J id) {
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
    public Collection<T> findAll(T object) {
        return baseDAO.findAll(object);
    }

    public Collection<J> findAllOnlyIds(T filter) {
        return this.baseDAO.findAllOnlyIds(filter);
    }

    public Collection<T> getAllPageable(Collection<J> collection, Pagination pagination) {
        return this.baseDAO.getAllPageable(collection, pagination);
    }

    public Collection<T> getAllByIds(Collection<J> ids) {
        return this.baseDAO.getAllByIds(ids);
    }
}
