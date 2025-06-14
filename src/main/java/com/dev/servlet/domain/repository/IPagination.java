package com.dev.servlet.domain.repository;

import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;

/**
 * This interface is used to implement query in the application.
 *
 * @param <T> {@linkplain T} is the object
 */
public interface IPagination<T> {

    /**
     * Get all results with pagination
     *
     * @param pageRequest {@linkplain IPageRequest}
     * @return {@linkplain IPageable} with the results
     */
    IPageable<T> getAllPageable(IPageRequest<T> pageRequest);

    /**
     * Get all results with pagination and map to another type
     *
     * @param pageRequest {@linkplain IPageRequest}
     * @param mapper      {@linkplain Mapper} to map the results
     * @param <U>         the type to which the results will be mapped
     * @return {@linkplain IPageable} with the mapped results
     */
    <U> IPageable<U> getAllPageable(IPageRequest<T> pageRequest, Mapper<T, U> mapper);
}

