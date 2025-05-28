package com.dev.servlet.model;

import com.dev.servlet.persistence.IPageRequest;
import com.dev.servlet.persistence.IPageable;

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
}
