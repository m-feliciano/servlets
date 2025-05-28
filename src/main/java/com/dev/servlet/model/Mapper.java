package com.dev.servlet.model;

@FunctionalInterface
public interface Mapper<R, U> {

    /**
     * Maps R object to U object.
     *
     * @param object the request to map
     * @return the mapped user object
     */
    U map(R object);
}